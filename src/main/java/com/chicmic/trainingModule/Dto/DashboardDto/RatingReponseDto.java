package com.chicmic.trainingModule.Dto.DashboardDto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Builder
public class RatingReponseDto {
    private Double overall;
    private String comment;
    private Double presentation;
    private Double course;
    private Double test;
    private Double behaviour;
    private Double attendance;
}
