package com.chicmic.trainingModule.Service.FeedBackService;

import com.chicmic.trainingModule.Dto.FeedbackDto.MentorFeedbackRequestDto;
import com.chicmic.trainingModule.Dto.rating.Rating_MENTOR;
import com.chicmic.trainingModule.Entity.*;
import com.chicmic.trainingModule.ExceptionHandling.ApiException;
import com.chicmic.trainingModule.Repository.FeedbackRepo;
import com.chicmic.trainingModule.Service.AssignTaskService.AssignTaskService;
import com.chicmic.trainingModule.TrainingModuleApplication;
import com.chicmic.trainingModule.Util.TrimNullValidator.FeedbackType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import static com.chicmic.trainingModule.Service.FeedBackService.FeedbackService.compute_rating;

@Service
@RequiredArgsConstructor
public class MentorFeedbackService {
    private final FeedbackRepo feedbackRepo;
    private final AssignTaskService assignTaskService;
    public Boolean isUserAMentorOfTrainee(String userId, String traineeId) {
        AssignedPlan assignedPlan = assignTaskService.getAllAssignTasksByTraineeId(traineeId);
        List<Plan> plans = assignedPlan.getPlans();
        for (Plan plan : plans) {
            for (Phase<PlanTask> phase : plan.getPhases()) {
                for (PlanTask planTask : phase.getTasks()) {
                    if(planTask != null) {
                        if(planTask.getMentorIds() != null && planTask.getMentorIds().contains(userId)){
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
    private Feedback_V2 saveFeedback(Feedback_V2 feedback) {
        return feedbackRepo.save(feedback);
    }
    public List<Feedback_V2> getCurrentMonthFeedback(String mentorId, String createdBy) {
        // Calculate start and end dates of the current month
        LocalDate now = LocalDate.now();
        LocalDate firstDayOfMonth = now.withDayOfMonth(1);
        LocalDate lastDayOfMonth = now.withDayOfMonth(now.lengthOfMonth());

        // Convert LocalDate to Date
        Date startDate = Date.from(firstDayOfMonth.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(lastDayOfMonth.atStartOfDay(ZoneId.systemDefault()).toInstant());

        // Query the repository for feedback within the current month
        return feedbackRepo.findByMentorAndTypeAndCreatedByAndCreatedAtBetweenAndIsDeletedFalse(mentorId, FeedbackType.MENTOR_, createdBy, startDate, endDate);
    }
    public boolean checkValidRequest(MentorFeedbackRequestDto requestDto, String createdBy) {
        TrainingModuleApplication.searchUserById(createdBy);
        TrainingModuleApplication.searchUserById(requestDto.getMentor());
        if(requestDto.getMentor().equals(createdBy)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "You Are Not Allowed To Give Feedback to  YourSelf");
        }
        String mentorId = requestDto.getMentor();
        String traineeId = createdBy;
        if(!isUserAMentorOfTrainee(mentorId, traineeId)){
            throw new ApiException(HttpStatus.BAD_REQUEST, "The specified user is not a mentor of the Rater.");
        }
//        List<Feedback_V2> feedbackGiven = feedbackRepo.findByMentorAndTypeAndCreatedBy(mentorId, FeedbackType.MENTOR_, traineeId);
        List<Feedback_V2> feedbackGiven = getCurrentMonthFeedback(mentorId, traineeId);
        if (feedbackGiven != null && !feedbackGiven.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "You have already provided feedback to this user for the current month.");
        }

        return true;
    }

    public Feedback_V2 createFeedbackForMentor(MentorFeedbackRequestDto requestDto, String createdBy) {
        if(requestDto == null || createdBy == null || createdBy.isEmpty()) {
            return null;
        }
        checkValidRequest(requestDto, createdBy);
        Rating_MENTOR ratingMentor = Rating_MENTOR.builder()
                .technicalRating(requestDto.getTechnicalRating())
                .communicationRating(requestDto.getCommunicationRating())
                .attitudeRating(requestDto.getAttitudeRating())
                .build();
        Feedback_V2 feedbackV2 =  Feedback_V2.builder()
                .mentor(requestDto.getMentor())
                .type(FeedbackType.MENTOR_)
                .details(ratingMentor)
                .comment(requestDto.getComment())
                .createdAt(new Date())
                .updateAt(new Date())
                .createdBy(createdBy)
                .overallRating(compute_rating(requestDto.computeRating(),1))
                .isDeleted(false)
                .build();
        return saveFeedback(feedbackV2);
    }
    public Feedback_V2 updateFeedbackForMentor(MentorFeedbackRequestDto requestDto, String feedbackId, String updatedBy) {
        Feedback_V2 feedbackV2 = feedbackRepo.findById(feedbackId).orElse(null);
        if(feedbackV2 == null) {
           throw new ApiException(HttpStatus.BAD_REQUEST, "Feedback Not found");
        }
        if(requestDto == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Request is Empty");
        }
        try {
            Rating_MENTOR feedbackGiven = (Rating_MENTOR) feedbackV2.getDetails();
            if(requestDto.getTechnicalRating() != null) {
                feedbackGiven.setTechnicalRating(requestDto.getTechnicalRating());
            }
            if(requestDto.getCommunicationRating() != null) {
                feedbackGiven.setCommunicationRating(requestDto.getCommunicationRating());
            }
            if(requestDto.getAttitudeRating() != null) {
                feedbackGiven.setAttitudeRating(requestDto.getAttitudeRating());
            }
            if(requestDto.getComment() != null) {
                feedbackV2.setComment(requestDto.getComment());
            }
            feedbackV2.setDetails(feedbackGiven);
            return feedbackRepo.save(feedbackV2);
        } catch (Exception ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Feedback is invalid!");
        }
    }
    public Double calculateOverallRatingForMentor(String mentorId) {
        // Retrieve all feedbacks for the mentor from the repository
        List<Feedback_V2> feedbackList = feedbackRepo.findByMentorAndTypeAndIsDeletedFalse(mentorId, FeedbackType.MENTOR_);

        // Calculate overall rating
        if (feedbackList != null && !feedbackList.isEmpty()) {
            double totalRating = 0.0;
            int count = 0;
            for (Feedback_V2 feedback : feedbackList) {
                if (feedback.getOverallRating() != null) {
                    totalRating += feedback.getOverallRating();
                    count++;
                }
            }
            if (count > 0) {
                return totalRating / count;
            }
        }
        // Return null if no feedback or no ratings available
        return null;
    }
}
