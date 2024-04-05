package com.chicmic.trainingModule.Dto.DashboardDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Builder @Getter @Setter
public class PlanDto implements Comparable<PlanDto>{
    private String name;
    private Instant date;
    private String taskName;
    private Integer type;
    private List<Object> subtasks;
    private Integer extraConsumedTime;
    private Integer estimatedTime;
    private Integer consumedTime;
    @JsonProperty("isComplete")
    private boolean isComplete;

    @Override
    public int compareTo(PlanDto o) {
        int a = 1 - this.getType();
        int b = 1 - o.getType();
        return Integer.compare(a,b);
    }
}
