package com.chicmic.trainingModule.Dto.DashboardDto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
@Getter @Setter @Builder
public class FeedbackResponseDto {
    private String name;
    private String date;
    private Float rating;
    private String feedback;
}
