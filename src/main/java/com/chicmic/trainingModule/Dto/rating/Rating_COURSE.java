package com.chicmic.trainingModule.Dto.rating;

import com.chicmic.trainingModule.Util.FeedbackUtil;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
@Builder
@Getter
@Setter
public class Rating_COURSE implements Rating{
    private String courseId;
    private Double theoreticalRating;
    private Double technicalRating;
    private Double communicationRating;
    public Double computeOverallRating(){
        double total = theoreticalRating + technicalRating + communicationRating;
        return total/3;
    }
}
