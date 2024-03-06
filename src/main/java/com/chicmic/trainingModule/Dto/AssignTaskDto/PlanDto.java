package com.chicmic.trainingModule.Dto.AssignTaskDto;

import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import lombok.*;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanDto {
    //    private Integer planType;
    private String name;
    private String _id;
    private String assignPlanId;
    private List<UserIdAndNameDto> approver;
    private String consumedTime;
    private String estimatedTime;
    private Integer totalTasks;
    private Integer completedTasks;
    //    private String estimatedTime;
    private Double rating;
    private String feedbackId;
    private Set<UserIdAndNameDto> mentors;
    private Boolean isCompleted = null;
    private List<MilestoneDto> milestones;
    private Boolean isDeleted = false;
    private Boolean isApproved = false;
}
