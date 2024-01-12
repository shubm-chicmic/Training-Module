package com.chicmic.trainingModule.Service.AssignTaskService;


import com.chicmic.trainingModule.Dto.AssignTaskDto.AssignTaskResponseDto;
import com.chicmic.trainingModule.Dto.AssignTaskDto.PlanDto;
import com.chicmic.trainingModule.Dto.AssignTaskDto.PlanTaskResponseDto;
import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.chicmic.trainingModule.Entity.*;
import com.chicmic.trainingModule.Entity.Constants.EntityType;
import com.chicmic.trainingModule.Entity.Constants.ProgessConstants;
import com.chicmic.trainingModule.Service.CourseServices.CourseService;
//import com.chicmic.trainingModule.Service.FeedBackService.FeedbackService;
import com.chicmic.trainingModule.Service.FeedBackService.FeedbackService_V2;
import com.chicmic.trainingModule.Service.PhaseService;
import com.chicmic.trainingModule.Service.UserProgressService.UserProgressService;
import com.chicmic.trainingModule.TrainingModuleApplication;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssignTaskResponseMapper {
    private final FeedbackService_V2 feedbackServiceV2;
    private final UserProgressService userProgressService;
    private final PhaseService phaseService;
    private final CourseService courseService;
//    public List<AssignTaskResponseDto> mapAssignTaskToResponseDto(List<AssignedPlan> assignTasks, String traineeId, Principal principal) {
//        List<AssignTaskResponseDto> assignTaskResponseDtoList = new ArrayList<>();
//        for (AssignedPlan assignTask : assignTasks) {
//            assignTaskResponseDtoList.add(mapAssignTaskToResponseDto(assignTask, traineeId, principal));
//        }
//        return assignTaskResponseDtoList;
//    }
    public AssignTaskResponseDto mapAssignTaskToResponseDto(AssignedPlan assignTask, String traineeId, Principal principal) {
        if (assignTask == null) {
            Object trainee = new UserIdAndNameDto(traineeId, TrainingModuleApplication.searchNameById(traineeId), feedbackServiceV2.computeOverallRatingOfTrainee(traineeId));

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
            trainee = new UserIdAndNameDto(traineeId, TrainingModuleApplication.searchNameById(traineeId));
        }
     //userProgressService.getTotalCompletedTasks(traineeId);
        List<PlanDto> plans = new ArrayList<>();
        Set<String> mentors = new HashSet<>();
        Integer countOfCompletedPlan = 0;
        for (Plan plan : assignTask.getPlans()) {
            if(plan != null && !plan.getDeleted()) {
                Integer completedTasks = userProgressService.getTotalSubTaskCompletedInPlan(traineeId,plan.get_id(),5);
                Integer totalTask = 0;
                for (Phase<PlanTask> phase : plan.getPhases()) {
                    for (PlanTask planTask : phase.getTasks()) {
                        if (planTask == null)continue;
                        mentors.addAll(planTask.getMentorIds());
                        if((planTask.getPlanType() != 3 && planTask.getPlanType() != 4)) {
                            if(planTask.getTotalTasks() == null) {
                                totalTask += 0;
                            }else {
                                totalTask += phaseService.countTotalSubtask(planTask.getMilestones());
                            }
                        }else {
                            UserProgress userProgress = userProgressService.getUserProgressByTraineeIdPlanIdAndPlanTaskId(traineeId, plan.get_id(), planTask.get_id(), planTask.getPlanType());
                            if(userProgress != null && userProgress.getStatus() == ProgessConstants.Completed){
                                completedTasks++;
                            }
                            totalTask++;
                        }
                    }
                }
                Boolean isCompleted = false;
                if(totalTask == completedTasks) {
                    if(userProgressService.getUserProgressByTraineeIdAndPlanId(traineeId, plan.get_id(), EntityType.PLAN) == null) {
                        UserProgress userProgress = UserProgress.builder()
                                .planId(plan.get_id())
                                .progressType(EntityType.PLAN)
                                .status(ProgessConstants.Completed)
                                .build();
                        userProgressService.createUserProgress(userProgress);
                    }
                    isCompleted = true;
                    countOfCompletedPlan++;
                }
                //add mentor name also
                Set<UserIdAndNameDto> mentorDetails = new HashSet<>();
                for (String mentorDetail : mentors) {
                    mentorDetails.add(new UserIdAndNameDto(mentorDetail, TrainingModuleApplication.searchNameById(mentorDetail)));
                }
                PlanDto planDto = PlanDto.builder()
                        .assignPlanId(assignTask.get_id())
                        .name(plan.getPlanName())
                        .isApproved(plan.getApproved())
                        .isDeleted(plan.getDeleted())
                        ._id(plan.get_id())
                        .consumedTime("00:00")
                        .estimatedTime(plan.getEstimatedTime())
                        .totalTasks(totalTask)
                        .completedTasks(completedTasks)
                        .approver(plan.getApproverDetails())
                        .mentors(mentorDetails)
//                    .feedbackId()
                        .isCompleted(isCompleted)
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
                .isCompleted(countOfCompletedPlan == plans.size())
                .plans(plans)
                .trainee(trainee)
                .date(assignTask.getDate())
                .build();
    }

}
