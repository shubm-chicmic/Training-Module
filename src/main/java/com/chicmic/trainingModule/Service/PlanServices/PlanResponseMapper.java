package com.chicmic.trainingModule.Service.PlanServices;

import com.chicmic.trainingModule.Dto.PlanDto.PlanResponseDto;
import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.chicmic.trainingModule.Entity.Plan;
import com.chicmic.trainingModule.Entity.PlanTask;
import com.chicmic.trainingModule.TrainingModuleApplication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PlanResponseMapper {
    public List<PlanResponseDto> mapPlanToResponseDto(List<Plan> plans, Boolean isMilestoneRequired) {
        List<PlanResponseDto> planResponseDtoList = new ArrayList<>();
        for (Plan plan : plans) {
            planResponseDtoList.add(mapPlanToResponseDto(plan));
        }
        return planResponseDtoList;
    }

    public PlanResponseDto mapPlanToResponseDto(Plan plan) {
//        List<UserIdAndNameDto> approver = Optional.ofNullable(plan.getApprover())
//                .map(approverIds -> approverIds.stream()
//                        .map(approverId -> {
//                            String name = TrainingModuleApplication.searchNameById(approverId);
//                            return new UserIdAndNameDto(approverId, name);
//                        })
//                        .collect(Collectors.toList())
//                )
//                .orElse(null);
//
//        List<UserIdAndNameDto> approvedBy = Optional.ofNullable(plan.getApprovedBy())
//                .map(approvedByIds -> approvedByIds.stream()
//                        .map(approverId -> {
//                            String name = TrainingModuleApplication.searchNameById(approverId);
//                            return new UserIdAndNameDto(approverId, name);
//                        })
//                        .collect(Collectors.toList())
//                )
//                .orElse(null);
//
//        String totalEstimatedTime = calculateTotalEstimatedTimeInPlan(plan.getPhases());
//        int noOfTasks = 0;
//        for (com.chicmic.trainingModule.Entity.Plan33.Phase phase : plan.getPhases()) {
//            noOfTasks += phase.getTasks().size();
//        }
//
//        List<com.chicmic.trainingModule.Entity.Plan33.Phase> phaseList = new ArrayList<>();
//        for (com.chicmic.trainingModule.Entity.Plan33.Phase phase : plan.getPhases()) {
//            com.chicmic.trainingModule.Entity.Plan33.Phase newPhase = new com.chicmic.trainingModule.Entity.Plan33.Phase();
//            List<PlanTask> planTaskList = new ArrayList<>();
//            long phaseHours = 0;
//            long phaseMinutes = 0;
//            long totalHours = 0;
//            long totalMinutes = 0;
//            for (PlanTask planTask : phase.getTasks()) {
//                PlanTask newPlanTask = new PlanTask();
//                newPlanTask.setPlanType(planTask.getPlanType());
//                newPlanTask.setPlan(planTask.getPlan());
//                if(planTask.getPlanType() == 1) {
//                    newPlanTask.setPlanName(courseService.getCourseById((String) planTask.getPlan()).getName());
//                }
//                else if(planTask.getPlanType() == 2){
//                    newPlanTask.setPlanName(testService.getTestById((String) planTask.getPlan()).getTestName());
//                }
//                newPlanTask.setIsCompleted(planTask.getIsCompleted());
//                newPlanTask.setEstimatedTime(planTask.getEstimatedTime());
//                newPlanTask.set_id(planTask.get_id());
//                Object milestone = null;
//                System.out.println("Plan type " + planTask.getPlanType());
//                if (planTask.getPlanType() != null && planTask.getPlanType() == 1) {
//                    System.out.println("Milestone: fet " + planTask.getPhases());
//                    milestone = (courseService.getCourseByPhaseIds((String) planTask.getPlan(), (List<Object>) planTask.getPhases()));
//                } else if (planTask.getPlanType() != null && planTask.getPlanType() == 2) {
//                    milestone = (testService.getTestByMilestoneIds((String) planTask.getPlan(), (List<Object>) planTask.getPhases()));
//                }
//                System.out.println("Milestone : " + milestone);
//                String[] timeParts = planTask.getEstimatedTime().split(":");
//                if (timeParts.length == 1) {
//                    phaseHours += (timeParts[0] != null && !timeParts[0].isEmpty()) ? Long.parseLong(timeParts[0]) : 0;
//                } else if (timeParts.length == 2) {
//                    phaseHours += (timeParts[0] != null && !timeParts[0].isEmpty()) ? Long.parseLong(timeParts[0]) : 0;
//                    phaseMinutes += (timeParts[1] != null && !timeParts[1].isEmpty()) ? Long.parseLong(timeParts[1]) : 0;
//                }
//                System.out.println("\u001B[31m milestone " + milestone + "\u001B[0m");
//                newPlanTask.setPhases(milestone);
//                newPlanTask.setMentor(planTask.getMentor());
//                planTaskList.add(newPlanTask);
//            }
//            totalHours += phaseHours + phaseMinutes / 60;
//            totalMinutes += phaseMinutes % 60;
//            newPhase.set_id(phase.get_id());
//            newPhase.setPhaseName(phase.getPhaseName());
//            newPhase.setEstimatedTime(String.format("%02d:%02d", totalHours, totalMinutes));
//            newPhase.setNoOfTasks(phase.getTasks().size());
//            newPhase.setTasks(planTaskList);
//            newPhase.setIsCompleted(phase.getIsCompleted());
//            phaseList.add(newPhase);
//        }
        return PlanResponseDto.builder()
                ._id(plan.get_id())
                .planName(plan.getPlanName())
                .description(plan.getDescription())
                .estimatedTime("")
                .noOfPhases(plan.getPhases().size())
                .noOfTasks(0)
                .approver(plan.getApproverDetails())
                .totalPhases(plan.getPhases().size())
                .phases(plan.getPhases())
                .deleted(plan.getDeleted())
                .approvedBy(plan.getApprovedByDetails())
                .approved(plan.getApproved())
                .createdBy(plan.getCreatedBy())
                .createdByName(TrainingModuleApplication.searchNameById(plan.getCreatedBy()))
                .createdAt(plan.getCreatedAt())
                .build();
    }
}
