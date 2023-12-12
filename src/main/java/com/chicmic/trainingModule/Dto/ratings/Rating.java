package com.chicmic.trainingModule.Dto.ratings;

import com.chicmic.trainingModule.Dto.FeedBackDto;
import com.chicmic.trainingModule.ExceptionHandling.ApiException;
import org.springframework.http.HttpStatus;

public interface Rating {
    public static Rating getRating(FeedBackDto feedBackDto){
        String feedBack_type = feedBackDto.getFeedBackTypeId();
        switch (feedBack_type){
            case "1" :
                return new Rating_COURSE(feedBackDto.getCourseId(),feedBackDto.getPhaseId(),feedBackDto.getTheoreticalRating(), feedBackDto.getTechnicalRating(), feedBackDto.getCommunicationRating());
            case "2" :
                return new Rating_TEST(feedBackDto.getTestId(), feedBackDto.getMilestoneId(), feedBackDto.getTheoreticalRating(), feedBackDto.getCodingRating(), feedBackDto.getCommunicationRating());
            case "3" :
                return new Rating_PPT(feedBackDto.getCommunicationRating(), feedBackDto.getTechnicalRating(), feedBackDto.getPresentationRating());
            case "4" :
                return new Rating_BEHAVIOUR(feedBackDto.getTeamSpiritRating(),feedBackDto.getAttitudeRating());
        }
        throw new ApiException(HttpStatus.BAD_REQUEST,"FeedBack_Type should be in b/w 1 to 4");
    }
}
