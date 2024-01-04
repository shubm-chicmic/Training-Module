package com.chicmic.trainingModule.Service.AssignTaskService;


import com.chicmic.trainingModule.Dto.AssignTaskDto.AssignTaskResponseDto;
import com.chicmic.trainingModule.Dto.AssignTaskDto.PlanDto;
import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.chicmic.trainingModule.Entity.*;

import com.chicmic.trainingModule.Service.UserProgressService.UserProgressService;
import com.chicmic.trainingModule.TrainingModuleApplication;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssignTaskResponseMapper {
    private final UserProgressService userProgressService;
//    public List<AssignTaskResponseDto> mapAssignTaskToResponseDto(List<AssignedPlan> assignTasks, String traineeId, Principal principal) {
//        List<AssignTaskResponseDto> assignTaskResponseDtoList = new ArrayList<>();
//        for (AssignedPlan assignTask : assignTasks) {
//            assignTaskResponseDtoList.add(mapAssignTaskToResponseDto(assignTask, traineeId, principal));
//        }
//        return assignTaskResponseDtoList;
//    }
    public AssignTaskResponseDto mapAssignTaskToResponseDto(AssignedPlan assignTask, String traineeId, Principal principal) {
        if (assignTask == null) {
            Object trainee = new UserIdAndNameDto(traineeId, TrainingModuleApplication.searchNameById(traineeId), 0f);

            return AssignTaskResponseDto.builder().trainee(trainee).build();
        }
        Object trainee = null;
        if (traineeId == null && traineeId.isEmpty()) {
//            trainee = Optional.ofNullable(assignTask.getUsers())
//                    .map(userIds -> userIds.stream()
//                            .map(userId -> {
//                                String name = TrainingModuleApplication.searchNameById(userId);
//                                return new UserIdAndNameDto(userId, name);
//                            })
//                            .collect(Collectors.toList())
//                    )
//                    .orElse(null);
        } else {
            trainee = new UserIdAndNameDto(traineeId, TrainingModuleApplication.searchNameById(traineeId), 0f);
        }
     //userProgressService.getTotalCompletedTasks(traineeId);
        List<PlanDto> plans = new ArrayList<>();
        for (Plan plan : assignTask.getPlans()) {
            if(plan != null) {
                Integer completedTasks = userProgressService.getTotalSubTaskCompletedInPlan(traineeId,plan.get_id(),5);

                PlanDto planDto = PlanDto.builder()
                        .assignPlanId(assignTask.get_id())
                        .name(plan.getPlanName())
                        .isApproved(plan.getApproved())
                        .isDeleted(plan.getDeleted())
                        ._id(plan.get_id())
                        .consumedTime("00:00")
                        .estimatedTime(plan.getEstimatedTime())
                        .totalTasks(plan.getTotalTasks())
                        .completedTasks(completedTasks)
                        .approver(plan.getApproverDetails())
//                    .feedbackId()
                        .isCompleted(false)
                        .rating(0f)
                        .build();
                plans.add(planDto);
            }
        }
        return AssignTaskResponseDto.builder()
                ._id(assignTask.get_id())
                .createdByName(TrainingModuleApplication.searchNameById(assignTask.getCreatedBy()))
                .createdBy(assignTask.getCreatedBy())
                .reviewers(assignTask.getReviewerDetails())
                .plans(plans)
//                .totalPhases(assignTask.getPlans().size())
//                .approved(assignTask.getApproved())
//                .deleted(assignTask.getDeleted())
//                .approvedBy(approvedBy)
                .trainee(trainee)
                .date(assignTask.getDate())
                .build();
    }

}
