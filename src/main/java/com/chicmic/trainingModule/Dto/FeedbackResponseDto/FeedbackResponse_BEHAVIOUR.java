package com.chicmic.trainingModule.Dto.FeedbackResponseDto;

import com.chicmic.trainingModule.Dto.FeedBackDto;
import com.chicmic.trainingModule.Dto.UserDto;
import com.chicmic.trainingModule.Dto.ratings.Rating_BEHAVIOUR;
import com.chicmic.trainingModule.Entity.Feedback;
import com.chicmic.trainingModule.TrainingModuleApplication;
import com.chicmic.trainingModule.Util.FeedbackUtil;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

import static com.chicmic.trainingModule.TrainingModuleApplication.searchUserById;

@Getter @Setter @Builder
public class FeedbackResponse_BEHAVIOUR implements FeedbackResponse{
    private String _id;
    private String employeeFullName;
    private String reviewer;
    private String employeeCode;
    private String team;
    private String type;
    private String traineeId;
    private Float teamSpiritRating;
    private Float attitudeRating;
    private Date createdOn;
    private Float rating;
    private String comment;
    private Integer feedbackType;
    public static FeedbackResponse buildFeedbackResponse(Feedback feedback){
        Rating_BEHAVIOUR rating_behaviour = (Rating_BEHAVIOUR) feedback.getRating();
        UserDto userDto = searchUserById(feedback.getTraineeID());
        int feedbackTypeId = feedback.getFeedbackType().charAt(0) - '1';

        return FeedbackResponse_BEHAVIOUR.builder()
                ._id(feedback.getId())
                .comment(feedback.getComment())
                .employeeFullName(userDto.getName())
                .team(userDto.getTeamName())
                .teamSpiritRating(rating_behaviour.getTeamSpiritRating())
                .traineeId(feedback.getTraineeID())
                .attitudeRating(rating_behaviour.getAttitudeRating())
                .createdOn(feedback.getCreatedAt())
                .employeeCode(userDto.getEmpCode())
                .reviewer(TrainingModuleApplication.searchNameById(feedback.getCreatedBy()))
                .type(FeedbackUtil.FEEDBACK_TYPE_CATEGORY[feedbackTypeId])
                .rating(feedback.getOverallRating())
                .feedbackType(4)
                .build();
    }
}
