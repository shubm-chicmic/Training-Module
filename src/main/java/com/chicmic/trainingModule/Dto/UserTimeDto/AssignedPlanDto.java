package com.chicmic.trainingModule.Dto.UserTimeDto;

import com.chicmic.trainingModule.Entity.SubTask;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssignedPlanDto {
    private String _id;
    private LocalDateTime dateTime;
    private List<PlanDto> projects;
    private PlanDto project;
    private List<SubTask> tasks;
    private SubTask task;
}
