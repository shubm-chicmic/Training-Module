package com.chicmic.trainingModule.Dto.AssignTaskDto;

import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.chicmic.trainingModule.Entity.Plan.Plan;
import com.chicmic.trainingModule.Entity.Test.Milestone;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignTaskResponseDto {

    private String _id;
    private Object trainee;
    private List<UserIdAndNameDto> reviewers;
    private int totalPhases;
    private List<PlanDto> plans;
//    private List<Milestone> milestones;
    private List<UserIdAndNameDto> approvedBy = new ArrayList<>();
    private String createdBy;
    private String createdByName;
    private Boolean deleted = false;
    private Boolean approved = false;
}
