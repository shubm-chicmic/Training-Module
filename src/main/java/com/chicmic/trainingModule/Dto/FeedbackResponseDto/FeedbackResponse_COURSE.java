package com.chicmic.trainingModule.Dto.FeedbackResponseDto;

import com.chicmic.trainingModule.Dto.UserDto;
import com.chicmic.trainingModule.Dto.ratings.Rating_BEHAVIOUR;
import com.chicmic.trainingModule.Dto.ratings.Rating_COURSE;
import com.chicmic.trainingModule.Entity.Feedback;
import com.chicmic.trainingModule.TrainingModuleApplication;
import com.chicmic.trainingModule.Util.FeedbackUtil;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

import static com.chicmic.trainingModule.TrainingModuleApplication.searchUserById;

@Getter @Setter @Builder
public class FeedbackResponse_COURSE implements FeedbackResponse{
    private String _id;
    private String employeeFullName;
    private String reviewer;
    private String employeeCode;
    private String team;
    private String type;
    private Integer feedbackType;
    private String taskName;
    private String traineeId;
    private String subTask;
    private Float theoreticalRating;
    private Float technicalRating;
    private Float communicationRating;
    private Date createdOn;
    private Float rating;
    private String comment;
    public static FeedbackResponse buildFeedbackResponse(Feedback feedback){
        Rating_COURSE rating_course = (Rating_COURSE) feedback.getRating();
        UserDto userDto = searchUserById(feedback.getTraineeID());
        int feedbackTypeId = feedback.getFeedbackType().charAt(0) - '1';

        return FeedbackResponse_COURSE.builder()
                ._id(feedback.getId())
                .comment(feedback.getComment())
                .employeeFullName(userDto.getName())
                .team(userDto.getTeamName())
                .theoreticalRating(rating_course.getTheoreticalRating())
                .technicalRating(rating_course.getTechnicalRating())
                .communicationRating(rating_course.getCommunicationRating())
                .traineeId(feedback.getTraineeID())
                .taskName(rating_course.getCourseId())
                .subTask(rating_course.getPhaseId())
                .createdOn(feedback.getCreatedAt())
                .employeeCode(userDto.getEmpCode())
                .reviewer(TrainingModuleApplication.searchNameById(feedback.getCreatedBy()))
                .type(FeedbackUtil.FEEDBACK_TYPE_CATEGORY[feedbackTypeId])
                .rating(feedback.getOverallRating())
                .feedbackType(1)
                .build();
    }
}
