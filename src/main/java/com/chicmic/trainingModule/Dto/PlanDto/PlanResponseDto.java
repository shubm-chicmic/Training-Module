package com.chicmic.trainingModule.Dto.PlanDto;

import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.chicmic.trainingModule.Entity.Plan.Phase;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanResponseDto {
    private String _id;
    private String planName;
    private String description;
    private String estimatedTime;
    private int noOfTasks;
    private int noOfPhases;
    private List<UserIdAndNameDto> approver;
    private int totalPhases;
    private List<Phase> phases;
    private List<UserIdAndNameDto> approvedBy = new ArrayList<>();
    private String createdBy;
    private String createdByName;
    private LocalDateTime createdAt;

    private Boolean deleted = false;
    private Boolean approved = false;
}

