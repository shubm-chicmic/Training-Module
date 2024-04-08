package com.chicmic.trainingModule.Dto.rating;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Getter
@Setter
@Builder
public class Rating_MENTOR implements Rating{
    private Double attitudeRating;
    private Double technicalRating;
    private Double communicationRating;
    public Double computeOverallRating(){
        double total = attitudeRating + technicalRating + communicationRating;
        return total/3;
    }
}
