package com.chicmic.trainingModule.Dto.FeedbackResponseDto;

import com.chicmic.trainingModule.Dto.UserDto;
import com.chicmic.trainingModule.Dto.ratings.Rating_PPT;
import com.chicmic.trainingModule.Entity.Feedback;
import com.chicmic.trainingModule.TrainingModuleApplication;
import com.chicmic.trainingModule.Util.FeedbackUtil;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

import static com.chicmic.trainingModule.TrainingModuleApplication.searchUserById;

@Getter @Setter @Builder
public class FeedbackResponse_PPT implements FeedbackResponse{
    private String _id;
    private String employeeFullName;
    private String reviewer;
    private String employeeCode;
    private String team;
    private String type;
    private String traineeId;
    private Float communicationRating;
    private Float technicalRating;
    private Float presentationRating;
    private Date createdOn;
    private Float rating;
    private String comment;
    private Integer feedbackType;
    public static FeedbackResponse buildFeedbackResponse(Feedback feedback){
        Rating_PPT rating_ppt = (Rating_PPT) feedback.getRating();
        UserDto userDto = searchUserById(feedback.getTraineeID());
        int feedbackTypeId = feedback.getFeedbackType().charAt(0) - '1';

        return FeedbackResponse_PPT.builder()
                ._id(feedback.getId())
                .comment(feedback.getComment())
                .employeeFullName(userDto.getName())
                .team(userDto.getTeamName())
                .communicationRating(rating_ppt.getCommunicationRating())
                .traineeId(feedback.getTraineeID())
                .technicalRating(rating_ppt.getTechnicalRating())
                .presentationRating(rating_ppt.getPresentationRating())
                .createdOn(feedback.getCreatedAt())
                .employeeCode(userDto.getEmpCode())
                .reviewer(TrainingModuleApplication.searchNameById(feedback.getCreatedBy()))
                .type(FeedbackUtil.FEEDBACK_TYPE_CATEGORY[feedbackTypeId])
                .rating(feedback.getOverallRating())
                .feedbackType(3)
                .build();
    }
}
