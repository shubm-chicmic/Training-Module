package com.chicmic.trainingModule.Dto.FeedbackResponseDto_V2;

import com.chicmic.trainingModule.Dto.UserDto;
import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.chicmic.trainingModule.Dto.rating.Rating_TEST;
import com.chicmic.trainingModule.Entity.Feedback_V2;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Set;

import static com.chicmic.trainingModule.TrainingModuleApplication.searchUserById;
import static com.chicmic.trainingModule.Util.TrimNullValidator.FeedbackType.TEST_;

@Builder
@Getter
@Setter
public class FeedbackResponse_TEST implements FeedbackResponse{
    private String _id;
    private UserDto reviewer;
    private UserDto trainee;
    private UserIdAndNameDto feedbackType;
    private UserIdAndNameDto task;
    private UserIdAndNameDto plan;
    private List<UserIdAndNameDto> subTask;
    private Float theoreticalRating;
    private Float codingRating;
    private Float communicationRating;
    private String createdOn;
    private Float rating;
    private String comment;
    private Float overallRating;
    public  static FeedbackResponse buildFeedback_V2Response(Feedback_V2 feedback){
        System.out.println("inside feedback response............");
        Rating_TEST rating_test = (Rating_TEST) feedback.getDetails();
        UserDto trainee = searchUserById(feedback.getTraineeId());
        UserDto reviewer = searchUserById(feedback.getCreatedBy());
        Set<String> subTaskIds = feedback.getMilestoneIds();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        
        return FeedbackResponse_TEST.builder()
                ._id(feedback.get_id())
                .reviewer(reviewer)
                .trainee(trainee)
                .comment(feedback.getComment())
                .theoreticalRating(rating_test.getTheoreticalRating())
                .codingRating(rating_test.getCodingRating())
                .communicationRating(rating_test.getCommunicationRating())
                .feedbackType(new UserIdAndNameDto(TEST_, "TEST"))
                .task(new UserIdAndNameDto(rating_test.getTestId(), rating_test.getTestId()))
                .subTask(subTaskIds.stream().map(id -> new UserIdAndNameDto(id,id)).toList())
                .createdOn(formatter.format(feedback.getCreatedAt()))
                .rating(rating_test.computeOverallRating())
                .build();
    }
}
