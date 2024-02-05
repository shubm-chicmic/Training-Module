package com.chicmic.trainingModule.Dto.UserTimeDto;

import com.chicmic.trainingModule.Entity.SubTask;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlanTaskDto {
    private String _id;
    private String planTaskId;
//    private String phaseId;
//    private String phaseName;
    private String name;
//    private String planName;
    private Integer planType;
    private Instant date;
//    private List<SubTask> subTasks;
//    private SubTask subTask;
}
