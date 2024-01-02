package com.chicmic.trainingModule.Dto.AssignTaskDto;

import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskDto {
    private UserIdAndNameDto mainTask;
    private String subtaskId;
    private String subtask;
    private String estimatedTime;
    private String reference;
    private Boolean isCompleted;

}
