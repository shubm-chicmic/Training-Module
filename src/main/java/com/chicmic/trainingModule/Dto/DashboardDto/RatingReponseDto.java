package com.chicmic.trainingModule.Dto.DashboardDto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Builder
public class RatingReponseDto {
    private float overall;
    private String comment;
    private float presentation;
    private float course;
    private float test;
    private float behaviour;
    private float attendance;
}
