package com.chicmic.trainingModule.Dto.ratings;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor @Getter @Setter
public class Rating_BEHAVIOUR implements Rating{
    private Float teamSpiritRating;
    private Float attitudeRating;
}
