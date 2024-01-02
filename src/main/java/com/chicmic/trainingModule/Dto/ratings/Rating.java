package com.chicmic.trainingModule.Dto.ratings;

import com.chicmic.trainingModule.Dto.FeedBackDto;
//import com.chicmic.trainingModule.Dto.ratings.Rating_COURSE;
import com.chicmic.trainingModule.ExceptionHandling.ApiException;
import org.springframework.http.HttpStatus;

public interface Rating {
    public static Rating getRating(FeedBackDto feedBackDto){
        String feedBack_type = feedBackDto.getFeedbackType();
        switch (feedBack_type){
//            case "1" :  {
//                return new Rating_COURSE(feedBackDto.getCourse(), feedBackDto.getPhase(), feedBackDto.getTheoreticalRating(), feedBackDto.getTechnicalRating(), feedBackDto.getCommunicationRating());
//            }
            case "2" : {
                return new Rating_TEST(feedBackDto.getTest(), feedBackDto.getMilestone(), feedBackDto.getTheoreticalRating(), feedBackDto.getCodingRating(), feedBackDto.getCommunicationRating());
            }
            case "3" : {
                return new Rating_PPT(feedBackDto.getCommunicationRating(), feedBackDto.getTechnicalRating(), feedBackDto.getPresentationRating(),feedBackDto.getCourse());
            }
            case "4" : {
                return new Rating_BEHAVIOUR(feedBackDto.getTeamSpiritRating(), feedBackDto.getAttitudeRating());
            }
        }
        throw new ApiException(HttpStatus.BAD_REQUEST,"FeedBack_Type should be in b/w 1 to 4");
    }
    public static Float computeOverallRating(FeedBackDto feedBackDto){
        String feedBack_type = feedBackDto.getFeedbackType();
        switch (feedBack_type){
            case "1" :
                return (feedBackDto.getTheoreticalRating() +  feedBackDto.getTechnicalRating() + feedBackDto.getCommunicationRating())/3;
            case "2" :
                return (feedBackDto.getTheoreticalRating() + feedBackDto.getCodingRating() + feedBackDto.getCommunicationRating()) / 3;
            case "3" :
                return (feedBackDto.getCommunicationRating() +  feedBackDto.getTechnicalRating() + feedBackDto.getPresentationRating()) / 3;
            case "4" :
                return (feedBackDto.getTeamSpiritRating() + feedBackDto.getAttitudeRating()) / 2;
        }
        //return overallRating;
        throw new ApiException(HttpStatus.BAD_REQUEST,"FeedBack_Type should be in b/w 1 to 4");
    }
}
