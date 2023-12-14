package com.chicmic.trainingModule.Dto.PhaseResponse;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter @Setter @Builder
public class PhaseResponse_TEST implements PhaseResponse{
    private String milestoneId;
    private String milestoneName;
    private Float overallRating;
    private Float communicationRating;
    private Float codingRating;
    private Float theoreticalRating;
    private String comment;
    private Date createdAt;
}
