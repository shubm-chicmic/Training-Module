package com.chicmic.trainingModule.Dto.FeedbackResponseDto_V2;

import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.chicmic.trainingModule.Entity.Feedback_V2;
import com.chicmic.trainingModule.ExceptionHandling.ApiException;
import org.springframework.http.HttpStatus;

import java.util.List;

import static com.chicmic.trainingModule.Util.TrimNullValidator.FeedbackType.*;

public interface FeedbackResponse {
    public UserIdAndNameDto getFeedbackType();
    public void setTask(UserIdAndNameDto task);
    public UserIdAndNameDto getTask();
    public List<UserIdAndNameDto> getSubTask();
    public void setPlan(UserIdAndNameDto plan);
    public UserIdAndNameDto getPlan();

    public static FeedbackResponse buildFeedbackResponse(Feedback_V2 feedback){
        String type = feedback.getType();
        switch (type){
            case VIVA_ :
                return FeedbackResponse_COURSE.buildFeedback_V2Response(feedback);
            case TEST_:
                return FeedbackResponse_TEST.buildFeedback_V2Response(feedback);
            case PPT_:
                return FeedbackResponse_PPT.buildFeedback_V2Response(feedback);
            case BEHAVIUOR_:
                return FeedbackResponse_BEHAVIOUR.buildFeedback_V2Response(feedback);
        }
        throw new ApiException(HttpStatus.BAD_REQUEST,"Please enter valid feedbackType.");
    }
}
