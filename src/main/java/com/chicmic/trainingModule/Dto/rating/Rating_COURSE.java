package com.chicmic.trainingModule.Dto.rating;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
@Builder
@Getter
@Setter
public class Rating_COURSE implements Rating{
    private String courseId;
    private double theoreticalRating;
    private double technicalRating;
    private double communicationRating;
    public double computeOverallRating(){
        double total = theoreticalRating + technicalRating + communicationRating;
        return total/3;
    }
}
