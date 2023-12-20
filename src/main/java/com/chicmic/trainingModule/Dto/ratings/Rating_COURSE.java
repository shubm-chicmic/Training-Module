package com.chicmic.trainingModule.Dto.ratings;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class Rating_COURSE implements Rating{
    private String courseId;
    private String phaseId;
    private Float theoreticalRating;
    private Float technicalRating;
    private Float communicationRating;
}
