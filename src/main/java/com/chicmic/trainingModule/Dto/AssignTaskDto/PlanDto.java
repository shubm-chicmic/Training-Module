package com.chicmic.trainingModule.Dto.AssignTaskDto;

import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanDto {
    private Integer planType;
    private String name;
    private String _id;
    private String assignPlanId;
    private List<UserIdAndNameDto> reviewers;
    private Integer noOfTopics;
    private String estimatedTime;
    private Float rating;
    private String feedbackId;
    private Boolean isCompleted = null;
    private List<MilestoneDto> milestones;
    private Boolean isDeleted = false;
    private Boolean isApproved = false;
}
