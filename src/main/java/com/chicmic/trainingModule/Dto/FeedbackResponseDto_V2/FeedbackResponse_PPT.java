package com.chicmic.trainingModule.Dto.FeedbackResponseDto_V2;

import com.chicmic.trainingModule.Dto.UserDto;
import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.chicmic.trainingModule.Dto.rating.Rating_PPT;
import com.chicmic.trainingModule.Entity.Feedback_V2;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import static com.chicmic.trainingModule.TrainingModuleApplication.searchUserById;

@Builder @Getter @Setter
public class FeedbackResponse_PPT implements FeedbackResponse{
    private String _id;
    private UserDto reviewer;
    private UserDto trainee;
    private UserIdAndNameDto feedbackType;
    private UserIdAndNameDto task;
    private String createdOn;
    private Float rating;
    private String comment;
    private Float communicationRating;
    private Float technicalRating;
    private Float presentationRating;
    private Float overallRating;
//
    public  static FeedbackResponse buildFeedback_V2Response(Feedback_V2 feedback){
        Rating_PPT rating_ppt = (Rating_PPT) feedback.getDetails();
        UserDto trainee = searchUserById(feedback.getTraineeId());
        UserDto reviewer = searchUserById(feedback.getCreatedBy());

        return FeedbackResponse_PPT.builder()
                ._id(feedback.get_id())
                .reviewer(reviewer)
                .trainee(trainee)
                .comment(feedback.getComment())
                .technicalRating(rating_ppt.getTechnicalRating())
                .communicationRating(rating_ppt.getCommunicationRating())
                .communicationRating(rating_ppt.getCommunicationRating())
                .feedbackType(new UserIdAndNameDto("4", feedback.getType()))
                .task(new UserIdAndNameDto(feedback.getDetails().getTaskId(),"PPT"))
                .createdOn(feedback.getCreatedAt())
                .rating(rating_ppt.computeOverallRating())
                .build();
    }
    @Override
    public List<UserIdAndNameDto> getSubTask() {
        return new ArrayList<>();
    }
}
