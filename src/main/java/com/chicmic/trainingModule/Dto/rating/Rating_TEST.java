package com.chicmic.trainingModule.Dto.rating;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class Rating_TEST implements Rating{
    private String testId;
    private Double theoreticalRating;
    private Double codingRating;
    private Double communicationRating;
    public Double computeOverallRating(){
        double total = communicationRating + theoreticalRating + codingRating;
        return total/3;
    }
}
