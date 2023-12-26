package com.chicmic.trainingModule.Dto.FeedbackResponseDto;


import com.chicmic.trainingModule.Dto.UserDto;
import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.chicmic.trainingModule.Dto.ratings.Rating_TEST;
import com.chicmic.trainingModule.Entity.Feedback;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

import static com.chicmic.trainingModule.TrainingModuleApplication.searchUserById;
import static com.chicmic.trainingModule.Util.FeedbackUtil.FEEDBACK_TYPE_CATEGORY;

@Setter @Getter @Builder
public class FeedbackResponse_TEST implements FeedbackResponse{
    private String _id;
    private UserDto reviewer;
    private UserDto trainee;
    private UserIdAndNameDto feedbackType;
    private UserIdAndNameDto task;
    private UserIdAndNameDto subTask;
    private Float theoreticalRating;
    private Float codingRating;
    private Float communicationRating;
    private Date createdOn;
    private Float rating;
    private String comment;
    private Float overallRating;
    public static FeedbackResponse buildFeedbackResponse(Feedback feedback){
        System.out.println("inside feedback response............");
        Rating_TEST rating_test = (Rating_TEST) feedback.getRating();
        UserDto trainee = searchUserById(feedback.getTraineeID());
        UserDto reviewer = searchUserById(feedback.getCreatedBy());
        int feedbackTypeId = feedback.getType().charAt(0) - '1';

        return FeedbackResponse_TEST.builder()
                ._id(feedback.getId())
                .reviewer(reviewer)
                .trainee(trainee)
                .comment(feedback.getComment())
                .theoreticalRating(rating_test.getTheoreticalRating())
                .codingRating(rating_test.getCodingRating())
                .communicationRating(rating_test.getCommunicationRating())
                .feedbackType(new UserIdAndNameDto("2",FEEDBACK_TYPE_CATEGORY[feedbackTypeId]))
                .task(new UserIdAndNameDto(rating_test.getTestId(), rating_test.getTestId()))
                .subTask(new UserIdAndNameDto(rating_test.getMilestoneId(),rating_test.getMilestoneId()))
                .createdOn(feedback.getCreatedAt())
                .rating(feedback.getOverallRating())
                .build();
    }

}
