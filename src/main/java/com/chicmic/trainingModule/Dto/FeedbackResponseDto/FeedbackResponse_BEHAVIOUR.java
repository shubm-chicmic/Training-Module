package com.chicmic.trainingModule.Dto.FeedbackResponseDto;

import com.chicmic.trainingModule.Dto.UserDto;
import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.chicmic.trainingModule.Dto.rating.Rating_BEHAVIOUR;
import com.chicmic.trainingModule.Entity.Feedback_V2;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static com.chicmic.trainingModule.TrainingModuleApplication.searchUserById;
import static com.chicmic.trainingModule.Util.TrimNullValidator.FeedbackType.BEHAVIUOR_;

@Getter @Setter @Builder
public class FeedbackResponse_BEHAVIOUR implements FeedbackResponse{
        private String _id;
    private UserDto reviewer;
    private UserDto trainee;
    private UserIdAndNameDto feedbackType;
    private UserIdAndNameDto plan;
    private String createdOn;
    private Double rating;
    private String comment;
    private Double teamSpiritRating;
    private Double attitudeRating;
    private Double overallRating;
    public static FeedbackResponse buildFeedback_V2Response(Feedback_V2 feedback){
        Rating_BEHAVIOUR rating_behaviour = (Rating_BEHAVIOUR) feedback.getDetails();
        UserDto trainee = searchUserById(feedback.getTraineeId());
        UserDto reviewer = searchUserById(feedback.getCreatedBy());
//        int feedbackTypeId = feedback.getType().charAt(0) - '1';
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        return FeedbackResponse_BEHAVIOUR.builder()
                ._id(feedback.get_id())
                .reviewer(reviewer)
                .trainee(trainee)
                .comment(feedback.getComment())
                .teamSpiritRating(rating_behaviour.getTeamSpiritRating())
                .attitudeRating(rating_behaviour.getAttitudeRating())
                .feedbackType(new UserIdAndNameDto(BEHAVIUOR_, "BEHAVIOUR"))
                .createdOn(formatter.format(feedback.getCreatedAt()))
                .rating(feedback.getOverallRating())
                .build();
    }
    @Override
    public UserIdAndNameDto getTask() {
        return null;
    }
    @Override
    public void setTask(UserIdAndNameDto task) {}

    @Override
    public List<UserIdAndNameDto> getSubTask() {
        return new ArrayList<>();
    }
}