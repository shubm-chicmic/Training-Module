package com.chicmic.trainingModule.Dto.AssignTaskDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskCompleteDto {
    private String assignTaskId;
    private String planId;
    private String milestone;
    private String subtaskId;
}
