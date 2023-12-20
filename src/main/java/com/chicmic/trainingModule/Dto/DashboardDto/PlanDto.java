package com.chicmic.trainingModule.Dto.DashboardDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
@Builder @Getter @Setter
public class PlanDto {
    private String name;
    private String date;
    private String phase;
    @JsonProperty("isComplete")
    private boolean isComplete;
}
