package com.chicmic.trainingModule.Dto.PhaseResponse;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter @Setter @Builder
public class PhaseResponse_COURSE implements PhaseResponse{
    private String phaseId;
    private String phaseName;
    private Float overallRating;
    private Float communicationRating;
    private Float technicalRating;
    private Float theoreticalRating;
    private String comment;
    private Date createAt;
}
