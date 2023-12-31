package com.chicmic.trainingModule.Dto.DashboardDto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Builder
@Getter @Setter
public class DashboardResponse {
    private String name;
    private List<FeedbackResponseDto> feedbacks;
    private RatingReponseDto rating;
    private List<CourseDto> courses;
    private List<PlanDto> plan;
}
