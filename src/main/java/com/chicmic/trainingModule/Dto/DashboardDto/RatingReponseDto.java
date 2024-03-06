package com.chicmic.trainingModule.Dto.DashboardDto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RatingReponseDto {
    private double overall;
    private String comment;
    private double presentation;
    private double course;
    private double test;
    private double behaviour;
    private double attendance;
}
