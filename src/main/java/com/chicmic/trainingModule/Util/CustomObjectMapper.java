package com.chicmic.trainingModule.Util;

import com.chicmic.trainingModule.Dto.AssignTaskDto.AssignTaskResponseDto;
import com.chicmic.trainingModule.Dto.CourseDto.CourseResponseDto;
import com.chicmic.trainingModule.Dto.GithubSampleDto.GithubSampleResponseDto;
import com.chicmic.trainingModule.Dto.PlanDto.PlanResponseDto;
import com.chicmic.trainingModule.Dto.SessionDto.MomMessageResponseDto;
import com.chicmic.trainingModule.Dto.SessionDto.SessionResponseDto;
import com.chicmic.trainingModule.Dto.TestDto.TestResponseDto;
import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.chicmic.trainingModule.Entity.AssignTask.AssignTask;
import com.chicmic.trainingModule.Entity.Course.Course;
import com.chicmic.trainingModule.Entity.Course.CourseSubTask;
import com.chicmic.trainingModule.Entity.Course.CourseTask;
import com.chicmic.trainingModule.Entity.GithubSample.GithubSample;
import com.chicmic.trainingModule.Entity.Course.Phase;
import com.chicmic.trainingModule.Entity.Plan.Plan;
import com.chicmic.trainingModule.Entity.Plan.Task;
import com.chicmic.trainingModule.Entity.Session.Session;
import com.chicmic.trainingModule.Entity.Test.Milestone;
import com.chicmic.trainingModule.Entity.Test.Test;
import com.chicmic.trainingModule.Entity.Test.TestSubTask;
import com.chicmic.trainingModule.Entity.Test.TestTask;
import com.chicmic.trainingModule.Service.CourseServices.CourseService;
import com.chicmic.trainingModule.Service.TestServices.TestService;
import com.chicmic.trainingModule.TrainingModuleApplication;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CustomObjectMapper {
    private  final CourseService courseService;
    private  final TestService testService;
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
        }
        catch (JsonMappingException e) {
            throw new RuntimeException(e);
        }
    }
    public static List<SessionResponseDto> mapSessionToResponseDto(List<Session> sessions) {
        List<SessionResponseDto> sessionResponseDtoList = new ArrayList<>();
        for (Session session : sessions) {
            sessionResponseDtoList.add(mapSessionToResponseDto(session));
        }
        return sessionResponseDtoList;
    }
    public static SessionResponseDto mapSessionToResponseDto(Session session) {
        List<UserIdAndNameDto> teams = session.getTeams().stream()
                .map(teamId -> {
                    String name = TrainingModuleApplication.searchTeamById(teamId);
                    return new UserIdAndNameDto(teamId, name);
                })
                .collect(Collectors.toList());

        List<UserIdAndNameDto> trainees = session.getTrainees().stream()
                .map(traineeId -> {
                    String name = TrainingModuleApplication.searchNameById(traineeId);
                    return new UserIdAndNameDto(traineeId, name);
                })
                .collect(Collectors.toList());

        List<UserIdAndNameDto> sessionBy = session.getSessionBy().stream()
                .map(sessionById -> {
                    String name = TrainingModuleApplication.searchNameById(sessionById);
                    return new UserIdAndNameDto(sessionById, name);
                })
                .collect(Collectors.toList());

        List<UserIdAndNameDto> approver = session.getApprover().stream()
                .map(approverId -> {
                    String name = TrainingModuleApplication.searchNameById(approverId);
                    return new UserIdAndNameDto(approverId, name);
                })
                .collect(Collectors.toList());

        List<MomMessageResponseDto> MommessageList = session.getMOM().stream()
                .map(momMessage -> {
                    String name = TrainingModuleApplication.searchNameById(momMessage.get_id());
                    return new MomMessageResponseDto(momMessage.get_id(), name, momMessage.getMessage());
                })
                .collect(Collectors.toList());
        List<UserIdAndNameDto> approvedBy = session.getApprovedBy().stream()
                .map(approverId -> {
                    String name = TrainingModuleApplication.searchNameById(approverId);
                    return new UserIdAndNameDto(approverId, name);
                })
                .collect(Collectors.toList());
        System.out.println("approvedBy: " + approvedBy);
        return SessionResponseDto.builder()
                ._id(session.get_id())
                .MOM(MommessageList)
                .isDeleted(session.isDeleted())
                .location(session.getLocation())
                .locationName(TrainingModuleApplication.zoneCategoryMap.get(Integer.valueOf(session.getLocation())))
                .status(session.getStatus())
                .time(DateTimeUtil.getTimeFromDate(session.getDateTime()))
                .date(DateTimeUtil.getDateFromDate(session.getDateTime()))
                .dateTime(session.getDateTime())
                .title(session.getTitle())
                .teams(teams)
                .trainees(trainees)
                .sessionBy(sessionBy)
                .approver(approver)
                .approvedBy(approvedBy)
                .isApproved(session.isApproved())
                .createdBy(session.getCreatedBy())
                .build();
    }


    public static List<GithubSampleResponseDto> mapGithubSampleToResponseDto(List<GithubSample> githubSamples) {
        List<GithubSampleResponseDto> githubSampleResponseDtoList = new ArrayList<>();
        for (GithubSample githubSample : githubSamples) {
            githubSampleResponseDtoList.add(mapGithubSampleToResponseDto(githubSample));
        }
        return githubSampleResponseDtoList;
    }

    public static GithubSampleResponseDto mapGithubSampleToResponseDto(GithubSample githubSample) {
        List<UserIdAndNameDto> teams = githubSample.getTeams().stream()
                .map(teamId -> {
                    String name = TrainingModuleApplication.searchTeamById(teamId);
                    return new UserIdAndNameDto(teamId, name);
                })
                .collect(Collectors.toList());

        List<UserIdAndNameDto> createdBy = githubSample.getRepoCreatedBy().stream()
                .map(createdById -> {
                    String name = TrainingModuleApplication.searchNameById(createdById);
                    return new UserIdAndNameDto(createdById, name);
                })
                .collect(Collectors.toList());

        List<UserIdAndNameDto> approver = githubSample.getApprover().stream()
                .map(approverId -> {
                    String name = TrainingModuleApplication.searchNameById(approverId);
                    return new UserIdAndNameDto(approverId, name);
                })
                .collect(Collectors.toList());

        List<UserIdAndNameDto> approvedBy = githubSample.getApprovedBy().stream()
                .map(approverId -> {
                    String name = TrainingModuleApplication.searchNameById(approverId);
                    return new UserIdAndNameDto(approverId, name);
                })
                .collect(Collectors.toList());

        return GithubSampleResponseDto.builder()
                ._id(githubSample.get_id())
                .url(githubSample.getUrl())
                .deleted(githubSample.getIsDeleted())
                .projectName(githubSample.getProjectName())
                .comment(githubSample.getComment())
                .teams(teams)
                .repoCreatedBy(createdBy)
                .approver(approver)
                .approvedBy(approvedBy)
                .approved(githubSample.getIsApproved())
                .createdBy(githubSample.getCreatedBy())
                .build();
    }
    public static List<CourseResponseDto> mapCourseToResponseDto(List<Course> courses, Boolean isPhaseRequired) {
        List<CourseResponseDto> courseResponseDtoList = new ArrayList<>();
        if(isPhaseRequired) {
            for (Course course : courses) {
                courseResponseDtoList.add(mapCourseToResponseDto(course));
            }
        }else{
            for (Course course : courses) {
                courseResponseDtoList.add(mapCourseToResponseDtoForNoPhase(course));
            }
        }
        return courseResponseDtoList;
    }
    public static  String calculateTotalEstimatedTime(List<Phase> phases) {

        long totalHours = 0;
        long totalMinutes = 0;
        for (Phase phase : phases) {
                long phaseHours = 0;
                long phaseMinutes = 0;

                for (CourseTask courseTask : phase.getTasks()) {
                    for (CourseSubTask courseSubTask : courseTask.getSubtasks()) {

//                        System.out.println("Estimated time : " + subTask.getEstimatedTime());
                        String[] timeParts = courseSubTask.getEstimatedTime().split(":");
                        if (timeParts.length == 1) {
                            phaseHours += (timeParts[0] != null && !timeParts[0].isEmpty()) ? Long.parseLong(timeParts[0]) : 0;
                        } else if (timeParts.length == 2) {
                            phaseHours += (timeParts[0] != null && !timeParts[0].isEmpty()) ? Long.parseLong(timeParts[0]) : 0;
                            phaseMinutes += (timeParts[1] != null && !timeParts[1].isEmpty()) ? Long.parseLong(timeParts[1]) : 0;
                        }
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


    public static CourseResponseDto mapCourseToResponseDto(Course course) {
        List<UserIdAndNameDto> approver = Optional.ofNullable(course.getReviewers())
                .map(approverIds -> approverIds.stream()
                        .map(approverId -> {
                            String name = TrainingModuleApplication.searchNameById(approverId);
                            return new UserIdAndNameDto(approverId, name);
                        })
                        .collect(Collectors.toList())
                )
                .orElse(null);

        List<UserIdAndNameDto> approvedBy = Optional.ofNullable(course.getApprovedBy())
                .map(approvedByIds -> approvedByIds.stream()
                        .map(approverId -> {
                            String name = TrainingModuleApplication.searchNameById(approverId);
                            return new UserIdAndNameDto(approverId, name);
                        })
                        .collect(Collectors.toList())
                )
                .orElse(null);

        String totalEstimatedTime = calculateTotalEstimatedTime(course.getPhases());
        int noOfTopics = 0;
        for (Phase phase : course.getPhases()) {
            for(CourseTask courseTask : phase.getTasks()) {
                noOfTopics += courseTask.getSubtasks().size();
            }
        }
        return CourseResponseDto.builder()
                ._id(course.get_id())
                .guidelines(course.getGuidelines())
                .courseName(course.getName())
                .estimatedTime(totalEstimatedTime)
                .noOfTopics(noOfTopics)
                .figmaLink(course.getFigmaLink())
                .reviewers(approver)
                .totalPhases(course.getPhases().size())
                .phases(course.getPhases())
                .deleted(course.getIsDeleted())
                .approvedBy(approvedBy)
                .approved(course.getIsApproved())
                .createdBy(course.getCreatedBy())
                .createdByName(TrainingModuleApplication.searchNameById(course.getCreatedBy()))
                .build();
    }
    public static CourseResponseDto mapCourseToResponseDtoForNoPhase(Course course) {
        List<UserIdAndNameDto> approver = course.getReviewers().stream()
                .map(approverId -> {
                    String name = TrainingModuleApplication.searchNameById(approverId);
                    return new UserIdAndNameDto(approverId, name);
                })
                .collect(Collectors.toList());
        List<UserIdAndNameDto> approvedBy = course.getApprovedBy().stream()
                .map(approverId -> {
                    String name = TrainingModuleApplication.searchNameById(approverId);
                    return new UserIdAndNameDto(approverId, name);
                })
                .collect(Collectors.toList());
        String totalEstimatedTime = calculateTotalEstimatedTime(course.getPhases());
        int noOfTopics = 0;
        for (Phase phase : course.getPhases()) {
            for(CourseTask courseTask : phase.getTasks()) {
                noOfTopics += courseTask.getSubtasks().size();
            }
        }
        return CourseResponseDto.builder()
                ._id(course.get_id())
                .guidelines(course.getGuidelines())
                .courseName(course.getName())
                .estimatedTime(totalEstimatedTime)
                .noOfTopics(noOfTopics)
                .figmaLink(course.getFigmaLink())
                .reviewers(approver)
                .totalPhases(course.getPhases().size())
                .deleted(course.getIsDeleted())
                .approvedBy(approvedBy)
                .approved(course.getIsApproved())
                .createdBy(course.getCreatedBy())
                .createdByName(TrainingModuleApplication.searchNameById(course.getCreatedBy()))
                .build();
    }
    public static  String calculateTotalEstimatedTimeInTest(List<Milestone> milestones) {

        long totalHours = 0;
        long totalMinutes = 0;
        for (Milestone milestone : milestones) {
            long phaseHours = 0;
            long phaseMinutes = 0;

            for (TestTask testTask : milestone.getTasks()) {
                for (TestSubTask testSubTask : testTask.getSubtasks()) {

//                        System.out.println("Estimated time : " + subTask.getEstimatedTime());
                    String[] timeParts = testSubTask.getEstimatedTime().split(":");
                    if (timeParts.length == 1) {
                        phaseHours += (timeParts[0] != null && !timeParts[0].isEmpty()) ? Long.parseLong(timeParts[0]) : 0;
                    } else if (timeParts.length == 2) {
                        phaseHours += (timeParts[0] != null && !timeParts[0].isEmpty()) ? Long.parseLong(timeParts[0]) : 0;
                        phaseMinutes += (timeParts[1] != null && !timeParts[1].isEmpty()) ? Long.parseLong(timeParts[1]) : 0;
                    }
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
    public static List<TestResponseDto> mapTestToResponseDto(List<Test> tests, Boolean isMilestoneRequired) {
        List<TestResponseDto> testResponseDtoList = new ArrayList<>();
        for (Test test : tests) {
                testResponseDtoList.add(mapTestToResponseDto(test));
        }
        return testResponseDtoList;
    }
    public static TestResponseDto mapTestToResponseDto(Test test) {
        List<UserIdAndNameDto> approver = Optional.ofNullable(test.getReviewers())
                .map(approverIds -> approverIds.stream()
                        .map(approverId -> {
                            String name = TrainingModuleApplication.searchNameById(approverId);
                            return new UserIdAndNameDto(approverId, name);
                        })
                        .collect(Collectors.toList())
                )
                .orElse(null);

        List<UserIdAndNameDto> approvedBy = Optional.ofNullable(test.getApprovedBy())
                .map(approvedByIds -> approvedByIds.stream()
                        .map(approverId -> {
                            String name = TrainingModuleApplication.searchNameById(approverId);
                            return new UserIdAndNameDto(approverId, name);
                        })
                        .collect(Collectors.toList())
                )
                .orElse(null);
        List<UserIdAndNameDto> teams = Optional.ofNullable(test.getTeams())
                .map(approvedByIds -> approvedByIds.stream()
                        .map(approverId -> {
                            String name = TrainingModuleApplication.searchTeamById(approverId);
                            return new UserIdAndNameDto(approverId, name);
                        })
                        .collect(Collectors.toList())
                )
                .orElse(null);

        String totalEstimatedTime = calculateTotalEstimatedTimeInTest(test.getMilestones());
        int noOfTopics = 0;
        for (Milestone milestone : test.getMilestones()) {
            for(TestTask testTask : milestone.getTasks()) {
                noOfTopics += testTask.getSubtasks().size();
            }
        }
        return TestResponseDto.builder()
                ._id(test.get_id())
                .testName(test.getTestName())
                .estimatedTime(totalEstimatedTime)
                .noOfMilestones(test.getMilestones().size())
                .noOfTopics(noOfTopics)
                .teams(teams)
                .reviewers(approver)
                .milestones(test.getMilestones())
                .deleted(test.getDeleted())
                .approvedBy(approvedBy)
                .approved(test.getApproved())
                .createdBy(test.getCreatedBy())
                .createdByName(TrainingModuleApplication.searchNameById(test.getCreatedBy()))
                .build();
    }
    public static  String calculateTotalEstimatedTimeInPlan(List<com.chicmic.trainingModule.Entity.Plan.Phase> phases) {

        long totalHours = 0;
        long totalMinutes = 0;
        for (com.chicmic.trainingModule.Entity.Plan.Phase phase : phases) {
            long phaseHours = 0;
            long phaseMinutes = 0;

            for (Task task : phase.getTasks()) {

//                        System.out.println("Estimated time : " + subTask.getEstimatedTime());
                    String[] timeParts = task.getEstimatedTime().split(":");
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
    public  List<PlanResponseDto> mapPlanToResponseDto(List<Plan> plans, Boolean isMilestoneRequired) {
        List<PlanResponseDto> planResponseDtoList = new ArrayList<>();
        for (Plan plan : plans) {
            planResponseDtoList.add(mapPlanToResponseDto(plan));
        }
        return planResponseDtoList;
    }

    public  PlanResponseDto mapPlanToResponseDto(Plan plan) {
        List<UserIdAndNameDto> approver = Optional.ofNullable(plan.getReviewers())
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
        for (com.chicmic.trainingModule.Entity.Plan.Phase phase : plan.getPhases()) {
            noOfTasks += phase.getTasks().size();
        }
        List<com.chicmic.trainingModule.Entity.Plan.Phase> phaseList = new ArrayList<>();
        for (com.chicmic.trainingModule.Entity.Plan.Phase phase : plan.getPhases()) {
            com.chicmic.trainingModule.Entity.Plan.Phase newPhase = new com.chicmic.trainingModule.Entity.Plan.Phase();
            List<Task> taskList = new ArrayList<>();
            for (Task task : phase.getTasks()) {
                Task newTask = new Task();
                newTask.setPlanType(task.getPlanType());
                newTask.setPlan(task.getPlan());
                newTask.setIsCompleted(task.getIsCompleted());
                newTask.setEstimatedTime(task.getEstimatedTime());
                newTask.set_id(task.get_id());
                Object milestone = null;
                if(task.getPlanType() != null && task.getPlanType() == 1){
                    System.out.println("Milestone: fet " + task.getMilestones());
                    milestone = (courseService.getCourseByPhaseIds(task.getPlan(), (List<Object>) task.getMilestones()));
                }else if(task.getPlanType() != null && task.getPlanType() == 2){
                    milestone = (testService.getTestByMilestoneIds(task.getPlan(), (List<Object>) task.getMilestones()));
                }
                newTask.setMilestones( milestone);
                taskList.add(newTask);
            }
            newPhase.set_id(phase.get_id());
            newPhase.setPhaseName(phase.getPhaseName());
            newPhase.setTasks(taskList);
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
                .reviewers(approver)
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
    public static List<AssignTaskResponseDto> mapAssignTaskToResponseDto(List<AssignTask> assignTasks, String traineeId) {
        List<AssignTaskResponseDto> assignTaskResponseDtoList = new ArrayList<>();
        for (AssignTask assignTask : assignTasks) {
            assignTaskResponseDtoList.add(mapAssignTaskToResponseDto(assignTask, traineeId));
        }
        return assignTaskResponseDtoList;
    }

    public static AssignTaskResponseDto mapAssignTaskToResponseDto(AssignTask assignTask, String traineeId) {
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
        if(traineeId == null && traineeId.isEmpty()) {
            trainee = Optional.ofNullable(assignTask.getUsers())
                    .map(userIds -> userIds.stream()
                            .map(userId -> {
                                String name = TrainingModuleApplication.searchNameById(userId);
                                return new UserIdAndNameDto(userId, name);
                            })
                            .collect(Collectors.toList())
                    )
                    .orElse(null);
        }else {
            trainee = new UserIdAndNameDto(traineeId, TrainingModuleApplication.searchNameById(traineeId));
        }
//        for (Plan plan : assignTask.getPlans()) {
//            for (com.chicmic.trainingModule.Entity.Plan.Phase phase : plan.getPhases()){
//                System.out.println("");
//               phase.setTasks(null);
//            }
//        }
        return AssignTaskResponseDto.builder()
                ._id(assignTask.get_id())
                .createdByName(TrainingModuleApplication.searchNameById(assignTask.getCreatedBy()))
                .createdBy(assignTask.getCreatedBy())
                .reviewers(reviewers)
                .plans(assignTask.getPlans())
                .approvedBy(approvedBy)
                .trainee(trainee)
                .build();
    }

}

