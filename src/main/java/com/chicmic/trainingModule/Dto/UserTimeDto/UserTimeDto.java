package com.chicmic.trainingModule.Dto.UserTimeDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserTimeDto {
    //    @NotNull
//    private String traineeId;
    @NotNull
    private String planId;
    @NotNull
    @JsonProperty("planType")
    private Integer type;
    private String taskId;
    private String PlanTaskId;
    private String subTaskId;
    @NotNull
    private Integer consumedTime;

    @Override
    public String toString() {
        return "UserTimeDto{" +
                "planId='" + planId + '\'' +
                ", type=" + type +
                ", taskId='" + taskId + '\'' +
                ", PlanTaskId='" + PlanTaskId + '\'' +
                ", subTaskId='" + subTaskId + '\'' +
                ", consumedTime=" + consumedTime +
                '}';
    }
}