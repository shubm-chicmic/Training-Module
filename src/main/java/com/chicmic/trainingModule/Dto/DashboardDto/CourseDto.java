package com.chicmic.trainingModule.Dto.DashboardDto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
@Getter @Setter @Builder
public class CourseDto {
    private String name;
    private String planId;
    private Integer progress;
    private Integer consumedTime;
    private Integer estimatedTime;

    public CourseDto(String name, String planId, Integer progress, Integer consumedTime, Integer estimatedTime) {
        this.name = name;
        this.planId = planId;
        this.progress = progress;
        this.consumedTime = consumedTime;
        this.estimatedTime = estimatedTime;
    }
}
