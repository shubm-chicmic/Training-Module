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
    private String plan;
    private String milestone;
    private String mainTaskId;
    private String subtaskId;

    @Override
    public String toString() {
        return "TaskCompleteDto{" +
                "assignTaskId='" + assignTaskId + '\'' +
                ", planId='" + planId + '\'' +
                ", plan='" + plan + '\'' +
                ", milestone='" + milestone + '\'' +
                ", mainTaskId='" + mainTaskId + '\'' +
                ", subtaskId='" + subtaskId + '\'' +
                '}';
    }
}
