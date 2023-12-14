package com.chicmic.trainingModule.Dto.FeedbackResponseDto;


import com.chicmic.trainingModule.Dto.UserDto;
import com.chicmic.trainingModule.Dto.ratings.Rating_TEST;
import com.chicmic.trainingModule.Entity.Feedback;
import com.chicmic.trainingModule.TrainingModuleApplication;
import com.chicmic.trainingModule.Util.FeedbackUtil;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

import static com.chicmic.trainingModule.TrainingModuleApplication.searchUserById;

@Setter @Getter @Builder
public class FeedbackResponse_TEST implements FeedbackResponse{
    private String _id;
    private String employeeFullName;
    private String reviewer;
    private String employeeCode;
    private String team;
    private String type;
    private String taskName;
    private String subTask;
    private Float theoreticalRating;
    private Float codingRating;
    private Float communicationRating;
    private Date createdOn;
    private Float rating;
    private String traineeId;
    private String comment;
    private Integer feedbackType;
    public static FeedbackResponse buildFeedbackResponse(Feedback feedback){
        Rating_TEST rating_test = (Rating_TEST) feedback.getRating();
        UserDto userDto = searchUserById(feedback.getTraineeID());
        int feedbackTypeId = feedback.getFeedbackType().charAt(0) - '1';

        return FeedbackResponse_TEST.builder()
                ._id(feedback.getId())
                .comment(feedback.getComment())
                .employeeFullName(userDto.getName())
                .team(userDto.getTeamName())
                .theoreticalRating(rating_test.getTheoreticalRating())
                .codingRating(rating_test.getCodingRating())
                .communicationRating(rating_test.getCommunicationRating())
                .traineeId(feedback.getTraineeID())
                .taskName(rating_test.getTestId())
                .subTask(rating_test.getMilestoneId())
                .createdOn(feedback.getCreatedAt())
                .employeeCode(userDto.getEmpCode())
                .reviewer(TrainingModuleApplication.searchNameById(feedback.getCreatedBy()))
                .type(FeedbackUtil.FEEDBACK_TYPE_CATEGORY[feedbackTypeId])
                .rating(feedback.getOverallRating())
                .feedbackType(2)
                .build();
    }
}
