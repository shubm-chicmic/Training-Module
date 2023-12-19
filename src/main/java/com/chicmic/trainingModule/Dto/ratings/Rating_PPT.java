package com.chicmic.trainingModule.Dto.ratings;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class Rating_PPT implements Rating{
    private Float communicationRating;
    private Float technicalRating;
    private Float presentationRating;
    private String courseId;
}
