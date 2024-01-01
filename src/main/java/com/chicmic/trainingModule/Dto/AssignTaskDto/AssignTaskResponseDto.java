package com.chicmic.trainingModule.Dto.AssignTaskDto;

import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import lombok.*;

import java.time.LocalDateTime;
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
    private LocalDateTime date;
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
