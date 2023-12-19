package com.chicmic.trainingModule.Dto.CourseResponse;

import com.chicmic.trainingModule.Dto.PhaseResponse.PhaseResponse;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter @Builder
public class CourseResponse {
    private String _id;
    private String reviewerName;
    private String code;
    private Float overallRating;
    private List<PhaseResponse> records;
}
