package com.chicmic.trainingModule.Dto.AssignTaskDto;

import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskDto {
    private UserIdAndNameDto phase;
    private UserIdAndNameDto mainTask;
    private String subtaskId;
    private UserIdAndNameDto subTask;
    private String estimatedTime;
    private String reference;
    private Boolean isCompleted;

}
