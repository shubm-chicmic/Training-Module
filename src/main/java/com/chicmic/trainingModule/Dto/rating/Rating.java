package com.chicmic.trainingModule.Dto.rating;

import com.chicmic.trainingModule.Dto.FeedbackDto.FeedbackRequestDto;
import com.chicmic.trainingModule.ExceptionHandling.ApiException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Set;

public interface Rating {
//    public String getCourseId();

    default public String getCourseId(){
        return null;
    }

    default public String getTestId(){
        return null;
    }

    public Float computeOverallRating();

    static Rating getRating(FeedbackRequestDto feedBackDto) {
        String feedBack_type = feedBackDto.getFeedbackType();
        switch (feedBack_type) {
            case "1": {
                return Rating_COURSE.builder()
                        .courseId(feedBackDto.getCourse())
                        .theoreticalRating(feedBackDto.getTheoreticalRating())
                        .technicalRating(feedBackDto.getTechnicalRating())
                        .communicationRating(feedBackDto.getCommunicationRating())
                        .build();
            }
            case "2": {
                return Rating_TEST.builder()
                        .testId(feedBackDto.getTest())
                        .theoreticalRating(feedBackDto.getTheoreticalRating())
                        .codingRating(feedBackDto.getCodingRating())
                        .communicationRating(feedBackDto.getCommunicationRating())
                        .build();
            }
            case "3": {
                return Rating_BEHAVIOUR.builder()
                        .teamSpiritRating(feedBackDto.getTeamSpiritRating())
                        .attitudeRating(feedBackDto.getAttitudeRating())
                        .build();
            }
            case "4": {
                return Rating_PPT.builder()
                        .courseId(feedBackDto.getCourse())
                        .communicationRating(feedBackDto.getCommunicationRating())
                        .technicalRating(feedBackDto.getTechnicalRating())
                        .presentationRating(feedBackDto.getPresentationRating())
                        .build();
            }
        }
        throw new ApiException(HttpStatus.BAD_REQUEST, "FeedBack_Type should be in b/w 1 to 4");
    }

    static Set<String> getSubTaskIds(FeedbackRequestDto feedbackDto) {
        String feedBack_type = feedbackDto.getFeedbackType();
        switch (feedBack_type) {
            case "1":
                return feedbackDto.getPhase();
            case "2":
                return feedbackDto.getMilestone();
            default:
                return null;
        }
    }
}
