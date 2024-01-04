package com.chicmic.trainingModule.Dto.FeedbackResponseDto_V2;

import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.chicmic.trainingModule.Entity.Feedback_V2;
import com.chicmic.trainingModule.ExceptionHandling.ApiException;
import org.springframework.http.HttpStatus;

import java.util.List;

public interface FeedbackResponse {
    public UserIdAndNameDto getFeedbackType();
    public void setTask(UserIdAndNameDto task);
    public UserIdAndNameDto getTask();
    public List<UserIdAndNameDto> getSubTask();
    public static FeedbackResponse buildFeedbackResponse(Feedback_V2 feedback){
        String type = feedback.getType();
        switch (type){
            case "1" :
                return FeedbackResponse_COURSE.buildFeedback_V2Response(feedback);
            case "2" :
                return FeedbackResponse_TEST.buildFeedback_V2Response(feedback);
            case "4" :
                return FeedbackResponse_PPT.buildFeedback_V2Response(feedback);
            case "3":
                return FeedbackResponse_BEHAVIOUR.buildFeedback_V2Response(feedback);
        }
        throw new ApiException(HttpStatus.BAD_REQUEST,"Please enter valid feedbackType.");
    }
}
