package com.chicmic.trainingModule.Dto.UserTimeDto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlanDto {
    private String _id;
    private String name;
//    private String description;
//    private Integer estimatedTime;
//    private Integer totalTasks;
    private List<PlanTaskDto> milestones;

//    private String createdBy;
//    private Boolean deleted = false;
//    private LocalDateTime createdAt;
//    private LocalDateTime updatedAt;
}
