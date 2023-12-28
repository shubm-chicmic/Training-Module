package com.chicmic.trainingModule.Util;

import com.chicmic.trainingModule.Dto.AssignTaskDto.AssignTaskResponseDto;
import com.chicmic.trainingModule.Dto.AssignTaskDto.MilestoneDto;
import com.chicmic.trainingModule.Dto.AssignTaskDto.PlanDto;
import com.chicmic.trainingModule.Dto.CourseDto.CourseResponseDto;
import com.chicmic.trainingModule.Dto.GithubSampleDto.GithubSampleResponseDto;
import com.chicmic.trainingModule.Dto.PlanDto.PlanResponseDto;
import com.chicmic.trainingModule.Dto.SessionDto.MomMessageResponseDto;
import com.chicmic.trainingModule.Dto.SessionDto.SessionResponseDto;
import com.chicmic.trainingModule.Dto.TestDto.TestResponseDto;
import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.chicmic.trainingModule.Entity.AssignedPlan;
import com.chicmic.trainingModule.Entity.AssignTasktem.AssignTaskPlanTrack;
import com.chicmic.trainingModule.Entity.Course;
import com.chicmic.trainingModule.Entity.Course123.CourseSubTask;
import com.chicmic.trainingModule.Entity.Course123.CourseTask;
import com.chicmic.trainingModule.Entity.GithubSample;
import com.chicmic.trainingModule.Entity.Course123.Phase;
import com.chicmic.trainingModule.Entity.Plan;
import com.chicmic.trainingModule.Entity.PlanTask;
import com.chicmic.trainingModule.Entity.Session;
import com.chicmic.trainingModule.Entity.Test.Milestone;
import com.chicmic.trainingModule.Entity.Test;
import com.chicmic.trainingModule.Entity.Test.TestSubTask;
import com.chicmic.trainingModule.Entity.Test.TestTask;
import com.chicmic.trainingModule.Service.CourseServices.CourseService;
import com.chicmic.trainingModule.Service.FeedBackService.FeedbackService;
import com.chicmic.trainingModule.Service.TestServices.TestService;
import com.chicmic.trainingModule.TrainingModuleApplication;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CustomObjectMapper {
    private final CourseService courseService;
    private final FeedbackService feedbackService;
    private final TestService testService;

    public static <T> T convert(Object dto, Class<T> targetType) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return mapper.convertValue(dto, targetType);
    }

    public static Object updateFields(Object source, Object target) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            return mapper.updateValue(target, source);
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        }
    }
    public static String calculateTotalEstimatedTimeInPlan(List<com.chicmic.trainingModule.Entity.Plan33.Phase> phases) {

        long totalHours = 0;
        long totalMinutes = 0;
        for (com.chicmic.trainingModule.Entity.Plan33.Phase phase : phases) {
            long phaseHours = 0;
            long phaseMinutes = 0;

            for (PlanTask planTask : phase.getTasks()) {

//                        System.out.println("Estimated time : " + subTask.getEstimatedTime());
                String[] timeParts = planTask.getEstimatedTime().split(":");
                if (timeParts.length == 1) {
                    phaseHours += (timeParts[0] != null && !timeParts[0].isEmpty()) ? Long.parseLong(timeParts[0]) : 0;
                } else if (timeParts.length == 2) {
                    phaseHours += (timeParts[0] != null && !timeParts[0].isEmpty()) ? Long.parseLong(timeParts[0]) : 0;
                    phaseMinutes += (timeParts[1] != null && !timeParts[1].isEmpty()) ? Long.parseLong(timeParts[1]) : 0;
                }
            }


            totalHours += phaseHours + phaseMinutes / 60;
            totalMinutes += phaseMinutes % 60;

        }
        // Convert total hours and minutes to proper format
        totalHours += totalMinutes / 60;
        totalMinutes %= 60;

        return String.format("%02d:%02d", totalHours, totalMinutes);
    }

    public List<PlanResponseDto> mapPlanToResponseDto(List<Plan> plans, Boolean isMilestoneRequired) {
        List<PlanResponseDto> planResponseDtoList = new ArrayList<>();
        for (Plan plan : plans) {
            planResponseDtoList.add(mapPlanToResponseDto(plan));
        }
        return planResponseDtoList;
    }

    public PlanResponseDto mapPlanToResponseDto(Plan plan) {
        List<UserIdAndNameDto> approver = Optional.ofNullable(plan.getApprover())
                .map(approverIds -> approverIds.stream()
                        .map(approverId -> {
                            String name = TrainingModuleApplication.searchNameById(approverId);
                            return new UserIdAndNameDto(approverId, name);
                        })
                        .collect(Collectors.toList())
                )
                .orElse(null);

        List<UserIdAndNameDto> approvedBy = Optional.ofNullable(plan.getApprovedBy())
                .map(approvedByIds -> approvedByIds.stream()
                        .map(approverId -> {
                            String name = TrainingModuleApplication.searchNameById(approverId);
                            return new UserIdAndNameDto(approverId, name);
                        })
                        .collect(Collectors.toList())
                )
                .orElse(null);

        String totalEstimatedTime = calculateTotalEstimatedTimeInPlan(plan.getPhases());
        int noOfTasks = 0;
        for (com.chicmic.trainingModule.Entity.Plan33.Phase phase : plan.getPhases()) {
            noOfTasks += phase.getTasks().size();
        }

        List<com.chicmic.trainingModule.Entity.Plan33.Phase> phaseList = new ArrayList<>();
        for (com.chicmic.trainingModule.Entity.Plan33.Phase phase : plan.getPhases()) {
            com.chicmic.trainingModule.Entity.Plan33.Phase newPhase = new com.chicmic.trainingModule.Entity.Plan33.Phase();
            List<PlanTask> planTaskList = new ArrayList<>();
            long phaseHours = 0;
            long phaseMinutes = 0;
            long totalHours = 0;
            long totalMinutes = 0;
            for (PlanTask planTask : phase.getTasks()) {
                PlanTask newPlanTask = new PlanTask();
                newPlanTask.setPlanType(planTask.getPlanType());
                newPlanTask.setPlan(planTask.getPlan());
                if(planTask.getPlanType() == 1) {
                    newPlanTask.setPlanName(courseService.getCourseById((String) planTask.getPlan()).getName());
                }
                else if(planTask.getPlanType() == 2){
                    newPlanTask.setPlanName(testService.getTestById((String) planTask.getPlan()).getTestName());
                }
                newPlanTask.setIsCompleted(planTask.getIsCompleted());
                newPlanTask.setEstimatedTime(planTask.getEstimatedTime());
                newPlanTask.set_id(planTask.get_id());
                Object milestone = null;
                System.out.println("Plan type " + planTask.getPlanType());
                if (planTask.getPlanType() != null && planTask.getPlanType() == 1) {
                    System.out.println("Milestone: fet " + planTask.getPhases());
                    milestone = (courseService.getCourseByPhaseIds((String) planTask.getPlan(), (List<Object>) planTask.getPhases()));
                } else if (planTask.getPlanType() != null && planTask.getPlanType() == 2) {
                    milestone = (testService.getTestByMilestoneIds((String) planTask.getPlan(), (List<Object>) planTask.getPhases()));
                }
                System.out.println("Milestone : " + milestone);
                String[] timeParts = planTask.getEstimatedTime().split(":");
                if (timeParts.length == 1) {
                    phaseHours += (timeParts[0] != null && !timeParts[0].isEmpty()) ? Long.parseLong(timeParts[0]) : 0;
                } else if (timeParts.length == 2) {
                    phaseHours += (timeParts[0] != null && !timeParts[0].isEmpty()) ? Long.parseLong(timeParts[0]) : 0;
                    phaseMinutes += (timeParts[1] != null && !timeParts[1].isEmpty()) ? Long.parseLong(timeParts[1]) : 0;
                }
                System.out.println("\u001B[31m milestone " + milestone + "\u001B[0m");
                newPlanTask.setPhases(milestone);
                newPlanTask.setMentor(planTask.getMentor());
                planTaskList.add(newPlanTask);
            }
            totalHours += phaseHours + phaseMinutes / 60;
            totalMinutes += phaseMinutes % 60;
            newPhase.set_id(phase.get_id());
            newPhase.setPhaseName(phase.getPhaseName());
            newPhase.setEstimatedTime(String.format("%02d:%02d", totalHours, totalMinutes));
            newPhase.setNoOfTasks(phase.getTasks().size());
            newPhase.setTasks(planTaskList);
            newPhase.setIsCompleted(phase.getIsCompleted());
            phaseList.add(newPhase);
        }
        return PlanResponseDto.builder()
                ._id(plan.get_id())
                .planName(plan.getPlanName())
                .description(plan.getDescription())
                .estimatedTime(totalEstimatedTime)
                .noOfPhases(plan.getPhases().size())
                .noOfTasks(noOfTasks)
                .approver(approver)
                .totalPhases(plan.getPhases().size())
                .phases(phaseList)
                .deleted(plan.getDeleted())
                .approvedBy(approvedBy)
                .approved(plan.getApproved())
                .createdBy(plan.getCreatedBy())
                .createdByName(TrainingModuleApplication.searchNameById(plan.getCreatedBy()))
                .createdAt(plan.getCreatedAt())
                .build();
    }

    public List<AssignTaskResponseDto> mapAssignTaskToResponseDto(List<AssignedPlan> assignTasks, String traineeId, Principal principal) {
        List<AssignTaskResponseDto> assignTaskResponseDtoList = new ArrayList<>();
        for (AssignedPlan assignTask : assignTasks) {
            assignTaskResponseDtoList.add(mapAssignTaskToResponseDto(assignTask, traineeId, principal));
        }
        return assignTaskResponseDtoList;
    }

    public List<UserIdAndNameDto> mapIdsToNames(List<String> ids, String type) {
        return ids.stream()
                .map(id -> {
                    String name = "";
                    switch (type) {
                        case "user":
                            name = TrainingModuleApplication.searchNameById(id);
                            break;
                        case "team":
                            name = TrainingModuleApplication.searchTeamById(id);
                            break;
                        // Add cases for other types if required

                        default:
                            break;
                    }
                    return new UserIdAndNameDto(id, name);
                })
                .collect(Collectors.toList());
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

