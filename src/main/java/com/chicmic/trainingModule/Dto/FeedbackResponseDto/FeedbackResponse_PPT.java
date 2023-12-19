package com.chicmic.trainingModule.Dto.FeedbackResponseDto;

import com.chicmic.trainingModule.Dto.Reviewer;
import com.chicmic.trainingModule.Dto.UserDto;
import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.chicmic.trainingModule.Dto.ratings.Rating_PPT;
import com.chicmic.trainingModule.Dto.ratings.Rating_TEST;
import com.chicmic.trainingModule.Entity.Feedback;
import com.chicmic.trainingModule.TrainingModuleApplication;
import com.chicmic.trainingModule.Util.FeedbackUtil;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

import static com.chicmic.trainingModule.TrainingModuleApplication.searchUserById;
import static com.chicmic.trainingModule.Util.FeedbackUtil.FEEDBACK_TYPE_CATEGORY;

@Getter @Setter @Builder
public class FeedbackResponse_PPT implements FeedbackResponse{
    private String _id;
    private UserDto reviewer;
    private UserDto trainee;
    private UserIdAndNameDto feedbackType;
    private Date createdOn;
    private Float rating;
    private String comment;
    private Float communicationRating;
    private Float technicalRating;
    private Float presentationRating;

    public static FeedbackResponse buildFeedbackResponse(Feedback feedback){
        Rating_PPT rating_ppt = (Rating_PPT) feedback.getRating();
        UserDto trainee = searchUserById(feedback.getTraineeID());
        UserDto reviewer = searchUserById(feedback.getTraineeID());
        int feedbackTypeId = feedback.getType().charAt(0) - '1';

        return FeedbackResponse_PPT.builder()
                ._id(feedback.getId())
                .reviewer(reviewer)
                .trainee(trainee)
                .comment(feedback.getComment())
                .technicalRating(rating_ppt.getTechnicalRating())
                .communicationRating(rating_ppt.getCommunicationRating())
                .communicationRating(rating_ppt.getCommunicationRating())
                .feedbackType(new UserIdAndNameDto("1",FEEDBACK_TYPE_CATEGORY[feedbackTypeId]))
                .createdOn(feedback.getCreatedAt())
                .rating(feedback.getOverallRating())
                .build();
    }
}
