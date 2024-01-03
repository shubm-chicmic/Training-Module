package com.chicmic.trainingModule.Dto.PhaseResponse_V2;

import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter @Setter @Builder
public class PhaseResponse_V2 {
    private List<UserIdAndNameDto> subTask;
    private Float overallRating;
    private Float communicationRating;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Float codingRating;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Float theoreticalRating;
    private Float technicalRating;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Float presentationRating;
    private String comment;
    private String createdAt;
}
