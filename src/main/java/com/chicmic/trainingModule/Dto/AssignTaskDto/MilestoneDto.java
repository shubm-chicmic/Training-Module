package com.chicmic.trainingModule.Dto.AssignTaskDto;

import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MilestoneDto {
    private String _id;
    private String name;
    private String estimatedTime;
    private String noOfTasks;
    private List<UserIdAndNameDto> reviewers;
    private Boolean isCompleted = false;

    private List<TaskDto> tasks;
}
