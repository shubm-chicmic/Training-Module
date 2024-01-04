package com.chicmic.trainingModule.Dto.rating;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class Rating_TEST implements Rating{
    private String taskId;
    private Float theoreticalRating;
    private Float codingRating;
    private Float communicationRating;
    public Float computeOverallRating(){
        float total = communicationRating + theoreticalRating + codingRating;
        return total/3;
    }

}
