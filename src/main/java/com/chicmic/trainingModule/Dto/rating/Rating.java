package com.chicmic.trainingModule.Dto.rating;

import com.chicmic.trainingModule.Dto.FeedbackDto.FeedbackRequestDto;
import com.chicmic.trainingModule.ExceptionHandling.ApiException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Set;

import static com.chicmic.trainingModule.Util.TrimNullValidator.FeedbackType.*;

public interface Rating {
//    public String getCourseId();

    default public String getCourseId() {
        return null;
    }

    default public String getTestId() {
        return null;
    }

    public Double computeOverallRating();

    static Rating getRating(FeedbackRequestDto feedBackDto) {
        String feedBack_type = feedBackDto.getFeedbackType();
        switch (feedBack_type) {
            case VIVA_: {
                return Rating_COURSE.builder()
                        .courseId(feedBackDto.getCourse())
                        .theoreticalRating(feedBackDto.getTheoreticalRating())
                        .technicalRating(feedBackDto.getTechnicalRating())
                        .communicationRating(feedBackDto.getCommunicationRating())
                        .build();
            }
            case TEST_: {
                return Rating_TEST.builder()
                        .testId(feedBackDto.getTest())
                        .theoreticalRating(feedBackDto.getTheoreticalRating())
                        .codingRating(feedBackDto.getCodingRating())
                        .communicationRating(feedBackDto.getCommunicationRating())
                        .build();
            }
            case BEHAVIUOR_: {
                return Rating_BEHAVIOUR.builder()
                        .teamSpiritRating(feedBackDto.getTeamSpiritRating())
                        .attitudeRating(feedBackDto.getAttitudeRating())
                        .build();
            }
            case PPT_: {
                return Rating_PPT.builder()
                        .courseId(feedBackDto.getCourse())
                        .communicationRating(feedBackDto.getCommunicationRating())
                        .technicalRating(feedBackDto.getTechnicalRating())
                        .presentationRating(feedBackDto.getPresentationRating())
                        .build();
            }
        }
        throw new ApiException(HttpStatus.BAD_REQUEST, "FeedBack_Type should be in b/w 2 to 5");
    }

    static Set<String> getSubTaskIds(FeedbackRequestDto feedbackDto) {
        String feedBack_type = feedbackDto.getFeedbackType();
        switch (feedBack_type) {
            case VIVA_:
                return feedbackDto.getPhase();
            case TEST_:
                return feedbackDto.getMilestone();
            default:
                return null;
        }
    }
}
