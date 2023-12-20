package com.chicmic.trainingModule.Dto.DashboardDto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
@Getter @Setter @Builder
public class CourseDto {
    private String name;
    private Integer progress;
}
