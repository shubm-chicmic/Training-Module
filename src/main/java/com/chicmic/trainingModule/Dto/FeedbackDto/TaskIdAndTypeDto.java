package com.chicmic.trainingModule.Dto.FeedbackDto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TaskIdAndTypeDto {
    public String taskId;
    public String type;

    public TaskIdAndTypeDto(String taskId, String type) {
        this.taskId = taskId;
        this.type = type;
    }
}
