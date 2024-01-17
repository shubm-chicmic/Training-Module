package com.chicmic.trainingModule.Dto.AssignTaskDto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignTaskResponseDto {

    private String _id;
    private Object trainee;
    private String startDate;
//    private Integer completedTask;
//    private Integer totalTasks;
    private Boolean isCompleted;
    private List<PlanDto> plans;
//    private List<Milestone> milestones;
//    private List<UserIdAndNameDto> approvedBy = new ArrayList<>();
    private String createdBy;
    private String createdByName;
//    private Boolean deleted = false;
//    private Boolean approved = false;
}
