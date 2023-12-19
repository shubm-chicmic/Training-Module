package com.chicmic.trainingModule.Dto.PhaseResponse;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter @Setter @Builder
public class PhaseResponse {
    private String _id;
    private String name;
    private Float overallRating;
    private Float communicationRating;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Float codingRating;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Float theoreticalRating;
    private Float technicalRating;
    private String comment;
    private Date createdAt;
}

