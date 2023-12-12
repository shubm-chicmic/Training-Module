package com.chicmic.trainingModule.Dto.ratings;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class Rating_TEST implements Rating{
    private String testId;
    private String milestoneId;
    private Float theoreticalRating;
    private Float codingRating;
    private Float communicationRating;
}
