package com.chicmic.trainingModule.Dto.DashboardDto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Builder
public class RatingReponseDto {
    private Float overall;
    private String comment;
    private Float presentation;
    private Float course;
    private Float test;
    private Float behaviour;
    private Float attendance;
}
