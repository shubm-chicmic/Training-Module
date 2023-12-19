package com.chicmic.trainingModule.Dto.FeedbackResponseDto;

import com.chicmic.trainingModule.Entity.Feedback;
import com.chicmic.trainingModule.ExceptionHandling.ApiException;
import org.springframework.http.HttpStatus;

public interface FeedbackResponse {
    public static FeedbackResponse buildFeedbackResponse(Feedback feedback){
        String type = feedback.getType();
        switch (type){
            case "1" :
                return FeedbackResponse_COURSE.buildFeedbackResponse(feedback);
            case "2" :
                return FeedbackResponse_TEST.buildFeedbackResponse(feedback);
            case "3" :
                return FeedbackResponse_PPT.buildFeedbackResponse(feedback);
            case "4":
                return FeedbackResponse_BEHAVIOUR.buildFeedbackResponse(feedback);
        }
        throw new ApiException(HttpStatus.BAD_REQUEST,"Please enter valid feedbackType.");
    }

}
