package com.chicmic.trainingModule.Dto.CourseResponse_V2;

import com.chicmic.trainingModule.Dto.PhaseResponse.PhaseResponse;
import com.chicmic.trainingModule.Dto.PhaseResponse_V2.PhaseResponse_V2;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@Builder
public class CourseResponse_V2 {
    private String _id;
    private String reviewerName;
    private String code;
    private Float overallRating;
    private List<PhaseResponse_V2> records;
}
