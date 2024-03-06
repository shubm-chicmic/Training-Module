package com.chicmic.trainingModule.Dto.PhaseResponse;

import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class PhaseResponse {
    private List<UserIdAndNameDto> subTask;
    private Double overallRating;
    private Double communicationRating;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double codingRating;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double theoreticalRating;
    private Double technicalRating;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double presentationRating;
    private String comment;
    private String createdAt;
}
