package com.chicmic.trainingModule.Dto.rating;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class Rating_BEHAVIOUR implements Rating{
    private Float teamSpiritRating;
    private Float attitudeRating;

//    @Override
//    public String getTaskId() {
//        return null;
//    }
    public Float computeOverallRating(){
        Float total = teamSpiritRating + attitudeRating;
        return total/2;
    }
}
