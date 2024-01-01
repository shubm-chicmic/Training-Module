package com.chicmic.trainingModule.Service.AssignTaskService;

import com.chicmic.trainingModule.Dto.AssignTaskDto.AssignTaskResponseDto;
import com.chicmic.trainingModule.Dto.AssignTaskDto.MilestoneDto;
import com.chicmic.trainingModule.Dto.AssignTaskDto.PlanDto;
import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.chicmic.trainingModule.Entity.*;
import com.chicmic.trainingModule.TrainingModuleApplication;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AssignTaskResponseMapper {
    public List<AssignTaskResponseDto> mapAssignTaskToResponseDto(List<AssignedPlan> assignTasks, String traineeId, Principal principal) {
        List<AssignTaskResponseDto> assignTaskResponseDtoList = new ArrayList<>();
        for (AssignedPlan assignTask : assignTasks) {
            assignTaskResponseDtoList.add(mapAssignTaskToResponseDto(assignTask, traineeId, principal));
        }
        return assignTaskResponseDtoList;
    }
    public AssignTaskResponseDto mapAssignTaskToResponseDto(AssignedPlan assignTask, String traineeId, Principal principal) {
        if (assignTask == null) {
            Object trainee = new UserIdAndNameDto(traineeId, TrainingModuleApplication.searchNameById(traineeId), feedbackService.getOverallRatingOfTrainee(traineeId));

            return AssignTaskResponseDto.builder()
                    .trainee(trainee)
                    .build();
        }
        List<UserIdAndNameDto> reviewers = Optional.ofNullable(assignTask.getReviewers())
                .map(reviewersIds -> reviewersIds.stream()
                        .map(reviewerId -> {
                            String name = TrainingModuleApplication.searchNameById(reviewerId);
                            return new UserIdAndNameDto(reviewerId, name);
                        })
                        .collect(Collectors.toList())
                )
                .orElse(null);

        List<UserIdAndNameDto> approvedBy = Optional.ofNullable(assignTask.getApprovedBy())
                .map(approvedByIds -> approvedByIds.stream()
                        .map(approverId -> {
                            String name = TrainingModuleApplication.searchNameById(approverId);
                            return new UserIdAndNameDto(approverId, name);
                        })
                        .collect(Collectors.toList())
                )
                .orElse(null);
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
            trainee = new UserIdAndNameDto(traineeId, TrainingModuleApplication.searchNameById(traineeId), feedbackService.getOverallRatingOfTrainee(traineeId));
        }

        List<PlanDto> plans = new ArrayList<>();
        for (Plan plan : assignTask.getPlans()) {
            System.out.println(" plan = " + plan);
            for (com.chicmic.trainingModule.Entity.Plan33.Phase phase : plan.getPhases()) {
                for (PlanTask planTask : phase.getTasks()) {
                    PlanDto planDto = null;
                    if (planTask.getPlanType() == 1) {
                        Course course = courseService.getCourseById(((AssignTaskPlanTrack) planTask.getPlan()).get_id());
                        List<MilestoneDto> milestoneDtos = new ArrayList<>();
                        List<AssignTaskPlanTrack> milestonesData = (List<AssignTaskPlanTrack>) planTask.getPhases();
                        for(AssignTaskPlanTrack milestoneTrack : milestonesData) {
                            for (Phase coursePhase : course.getPhases()) {
                                if(milestoneTrack!=null && milestoneTrack.get_id().equals(coursePhase.get_id())){
                                    boolean isMilestoneCompleted = false;
                                    for (AssignTaskPlanTrack assignTaskPlanTrack : (List<AssignTaskPlanTrack>) planTask.getPhases()) {
                                        if (assignTaskPlanTrack.get_id().equals(coursePhase.get_id())) {
                                            isMilestoneCompleted = assignTaskPlanTrack.getIsCompleted();
                                            break;
                                        }
                                    }
                                    List<CourseTask> courseTasks = coursePhase.getTasks();
                                    for (CourseTask courseTask : courseTasks) {
                                        for (CourseSubTask courseSubTask : courseTask.getSubtasks()) {

                                            String subTaskId = courseSubTask.get_id();
                                            System.out.println("subtask id = " + subTaskId);

                                            for (AssignTaskPlanTrack milestonePlan : (List<AssignTaskPlanTrack>) planTask.getPhases()) {
//                                        if (milestonePlan.get_id().equals(phase.get_id())) {
                                                for (AssignTaskPlanTrack milestoneMainTask : milestonePlan.getTasks()) {
                                                    for (AssignTaskPlanTrack milestoneSubTask : milestoneMainTask.getSubtasks()) {
                                                        System.out.println("milestone Subtask id = " + milestoneSubTask.get_id());
                                                        if (milestoneSubTask.get_id().equals(subTaskId)) {
                                                            System.out.println("value of is complete = " + milestoneSubTask.getIsCompleted());
                                                            courseSubTask.setIsCompleted(milestoneSubTask.getIsCompleted());

                                                        }
                                                    }
                                                }
//                                        }
                                            }
                                        }

                                    }
                                    MilestoneDto milestoneDto = MilestoneDto.builder()
                                            ._id(coursePhase.get_id())
                                            .reviewers(planTask.getMentor())
                                            .name(coursePhase.getName())
                                            .feedbackId(feedbackService.getFeedbackIdForMileStoneAndPhase(String.valueOf(planTask.getPlanType()), ((AssignTaskPlanTrack) planTask.getPlan()).get_id(), coursePhase.get_id(), principal.getName(), assignTask.getUserId()))
                                            .isCompleted(isMilestoneCompleted)
                                            .noOfTasks(coursePhase.getTasks().size())
                                            .estimatedTime(coursePhase.getEstimatedTime())
                                            .tasks(coursePhase.getTasks())
                                            .build();
                                    milestoneDtos.add(milestoneDto);
                                }
                            }
                        }
                        planDto = PlanDto.builder()
                                ._id(course.get_id())
                                .planType(planTask.getPlanType())
                                .assignPlanId(plan.get_id())
                                .isCompleted(planTask.getIsCompleted())
//                                .feedbackId(feedbackService.getFeedbackIdForMileStoneAndPhase(String.valueOf(task.getPlanType()), (String)task.getPlan(), coursePhase.get_id(), principal.getName(), assignTask.getUserId()))
                                .estimatedTime(planTask.getEstimatedTime())
                                .noOfTopics(((List<AssignTaskPlanTrack>) planTask.getPhases()).size())
                                .isApproved(course.getIsApproved())
                                .isDeleted(course.getIsDeleted())
                                .milestones(milestoneDtos)
                                .name(course.getName())
                                .rating(0.0f)
                                .reviewers(planTask.getMentor())

                                .build();
                    } else if (planTask.getPlanType() == 2) {

                        Test test = testService.getTestById(((AssignTaskPlanTrack) planTask.getPlan()).get_id());
                        List<MilestoneDto> milestoneDtos = new ArrayList<>();
                        List<AssignTaskPlanTrack> milestonesData = (List<AssignTaskPlanTrack>) planTask.getPhases();
                        for(AssignTaskPlanTrack milestoneTrack : milestonesData) {
                            for (Milestone milestone : test.getMilestones()) {
                                if(milestoneTrack.get_id().equals(milestone.get_id())){
                                    boolean isMilestoneCompleted = false;
                                    for (AssignTaskPlanTrack assignTaskPlanTrack : (List<AssignTaskPlanTrack>) planTask.getPhases()) {
                                        if (assignTaskPlanTrack.get_id().equals(milestone.get_id())) {
                                            isMilestoneCompleted = assignTaskPlanTrack.getIsCompleted();
                                            break;
                                        }
                                    }
                                    List<TestTask> testTasks = milestone.getTasks();
                                    for (TestTask testTask : testTasks) {
                                        for (TestSubTask testSubTask : testTask.getSubtasks()) {

                                            String subTaskId = testSubTask.get_id();
                                            one:
                                            for (AssignTaskPlanTrack milestonePlan : (List<AssignTaskPlanTrack>) planTask.getPhases()) {
                                                if (milestonePlan.get_id().equals(milestone.get_id())) {
                                                    for (AssignTaskPlanTrack milestoneMainTask : milestonePlan.getTasks()) {
                                                        for (AssignTaskPlanTrack milestoneSubTask : milestoneMainTask.getSubtasks()) {
                                                            if (milestoneSubTask.get_id().equals(subTaskId)) {
                                                                testSubTask.setIsCompleted(milestoneSubTask.getIsCompleted());
                                                                break one;
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                    }
                                    MilestoneDto milestoneDto = MilestoneDto.builder()
                                            ._id(milestone.get_id())
                                            .reviewers(planTask.getMentor())

                                            .name(milestone.getName())
                                            .feedbackId(feedbackService.getFeedbackIdForMileStoneAndPhase(String.valueOf(planTask.getPlanType()), ((AssignTaskPlanTrack) planTask.getPlan()).get_id(), milestone.get_id(), principal.getName(), assignTask.getUserId()))
                                            .isCompleted(isMilestoneCompleted)
                                            .noOfTasks(milestone.getTasks().size())
                                            .estimatedTime(milestone.getEstimatedTime())
                                            .tasks(milestone.getTasks())
                                            .build();
                                    milestoneDtos.add(milestoneDto);
                                }
                            }
                        }
                        planDto = PlanDto.builder()
                                ._id(test.get_id())
                                .planType(planTask.getPlanType())
                                .assignPlanId(plan.get_id())
                                .isCompleted(planTask.getIsCompleted())
//                                .feedbackId("6581c5388096904d2af3204d")
                                .estimatedTime(planTask.getEstimatedTime())
                                .noOfTopics(((List<AssignTaskPlanTrack>) planTask.getPhases()).size())
                                .isApproved(test.getApproved())
                                .isDeleted(test.getDeleted())
                                .milestones(milestoneDtos)
                                .name(test.getTestName())
                                .rating(null)
                                .reviewers(planTask.getMentor())

                                .build();
                    } else if (planTask.getPlanType() == 4) {
                        Course course = courseService.getCourseById(((AssignTaskPlanTrack) planTask.getPlan()).get_id());
                        List<MilestoneDto> milestoneDtos = new ArrayList<>();
                        MilestoneDto milestoneDto = MilestoneDto.builder()
                                ._id(((AssignTaskPlanTrack) planTask.getPlan()).get_id())
                                .reviewers(planTask.getMentor())

                                .tasks(new ArrayList<>())
                                .name(course.getName())
                                .isCompleted(phase.getIsCompleted())
//                                .feedbackId(feedbackService.getFeedbackIdForMileStoneAndPhase(String.valueOf(task.getPlanType()), ((AssignTaskPlanTrack) task.getPlan()).get_id(), milestone.get_id(), principal.getName(), assignTask.getUserId()))
//                                .isCompleted(course)
                                .noOfTasks(course.getPhases().size())
                                .estimatedTime("08:00")
//                                .tasks(course)
                                .build();
                        milestoneDtos.add(milestoneDto);
                        planDto = PlanDto.builder()
                                ._id(((AssignTaskPlanTrack) planTask.getPlan()).get_id())
                                .planType(3)
                                .name("Ppt")
                                .assignPlanId(plan.get_id())
                                .isCompleted(phase.getIsCompleted())
                                .feedbackId("6581c5388096904d2af3204d")
                                .estimatedTime(planTask.getEstimatedTime())
                                .noOfTopics(((List<AssignTaskPlanTrack>) planTask.getPhases()).size())
                                .isApproved(false)
                                .isDeleted(false)
                                .milestones(milestoneDtos)
                                .rating(null)
                                .reviewers(planTask.getMentor())

                                .build();
                    }
                    plans.add(planDto);
                }
            }
        }
        System.out.println("Plans list finished = " + plans.size());
        System.out.println(plans);
        return AssignTaskResponseDto.builder()
                ._id(assignTask.get_id())
                .createdByName(TrainingModuleApplication.searchNameById(assignTask.getCreatedBy()))
                .createdBy(assignTask.getCreatedBy())
                .reviewers(reviewers)
                .plans(plans)
                .totalPhases(assignTask.getPlans().size())
                .approved(assignTask.getApproved())
                .deleted(assignTask.getDeleted())
                .approvedBy(approvedBy)
                .trainee(trainee)
                .date(assignTask.getDate())
                .build();
    }

}
