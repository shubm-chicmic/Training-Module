package com.chicmic.trainingModule.Dto.DashboardDto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Builder
public class CourseDto {
    private String name;
    private String planId;
    private Integer progress;

    public CourseDto(String name, String planId, Integer progress) {
        this.name = name;
        this.planId = planId;
        this.progress = progress;
    }
}
