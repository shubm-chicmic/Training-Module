package com.chicmic.trainingModule.Dto.FeedbackResponseDto_V2;

import com.chicmic.trainingModule.Dto.UserDto;
import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.chicmic.trainingModule.Dto.rating.Rating_COURSE;
import com.chicmic.trainingModule.Entity.Feedback_V2;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

import static com.chicmic.trainingModule.TrainingModuleApplication.searchUserById;

@Getter @Setter @Builder
public class FeedbackResponse_COURSE implements FeedbackResponse{
    private String _id;
    private UserDto reviewer;
    private UserDto trainee;
    private UserIdAndNameDto feedbackType;
    private UserIdAndNameDto task;
    private List<UserIdAndNameDto> subTask;
    private Float theoreticalRating;
    private Float technicalRating;
    private Float communicationRating;
    private String createdOn;
    private Float rating;
    private String comment;
    private Float overallRating;


    public void setSubTask(List<UserIdAndNameDto> subTask) {
        this.subTask = subTask;
    }

    public static FeedbackResponse buildFeedback_V2Response(Feedback_V2 feedback){
        Rating_COURSE rating_course = (Rating_COURSE) feedback.getDetails();
        UserDto trainee = searchUserById(feedback.getTraineeId());
        UserDto reviewer = searchUserById(feedback.getCreatedBy());
//        int feedbackTypeId = feedback.getType().charAt(0) - '1';
        Set<String> subTaskIds = feedback.getSubtaskIds();

        return FeedbackResponse_COURSE.builder()
                ._id(feedback.get_id())
                .reviewer(reviewer)
                .trainee(trainee)
                .comment(feedback.getComment())
                .theoreticalRating(rating_course.getTheoreticalRating())
                .technicalRating(rating_course.getTechnicalRating())
                .communicationRating(rating_course.getCommunicationRating())
                .feedbackType(new UserIdAndNameDto("1",feedback.getType()))
                .task(new UserIdAndNameDto(rating_course.getTaskId(), rating_course.getTaskId()))
                .subTask(subTaskIds.stream().map(id -> new UserIdAndNameDto(id,id)).toList())
                .createdOn(feedback.getCreatedAt())
                .rating(rating_course.computeOverallRating())
                .build();
    }
}