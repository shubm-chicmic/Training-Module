package com.chicmic.trainingModule.Dto.DashboardDto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
@Getter @Setter @Builder
public class CourseDto {
    private String name;
    private String planId;
    private String phaseId;
    private Integer progress;

    public CourseDto(String name,String planId ,String phaseId,Integer progress) {
        this.name = name;
        this.planId = planId;
        this.progress = progress;
    }
}
