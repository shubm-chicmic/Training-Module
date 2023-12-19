package com.chicmic.trainingModule.Dto.AssignTaskDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskDto {
    private String mainTaskId;
    private String mainTask;
    private String subtaskId;
    private String subtask;
    private String estimatedTime;
    private String reference;
    private Boolean isCompleted;

}
