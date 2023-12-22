package com.chicmic.trainingModule.Dto.DashboardDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
@Builder @Getter @Setter
public class PlanDto implements Comparable<PlanDto>{
    private String name;
    private String date;
    private String phase;
    private Integer type;
    @JsonProperty("isComplete")
    private boolean isComplete;

    @Override
    public int compareTo(PlanDto o) {
        int a = 1 - this.getType();
        int b = 1 - o.getType();
        return Integer.compare(a,b);
    }
}
