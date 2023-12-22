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
import com.chicmic.trainingModule.Entity.AssignTask.AssignTask;
import com.chicmic.trainingModule.Entity.AssignTask.AssignTaskPlanTrack;
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
import java.util.Collections;
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
        MomMessageResponseDto Mommessage = null;
        if(session.getMOM() != null){
            Mommessage = MomMessageResponseDto.builder()
                    ._id(session.getMOM().get_id())
                    .message(session.getMOM().getMessage())
                    .name(TrainingModuleApplication.searchNameById(session.getMOM().get_id()))
                    .build();
        }

        List<UserIdAndNameDto> approvedBy = session.getApprovedBy().stream()
                .map(approverId -> {
                    String name = TrainingModuleApplication.searchNameById(approverId);
                    return new UserIdAndNameDto(approverId, name);
                })
                .collect(Collectors.toList());
        System.out.println("approvedBy: " + approvedBy);
        return SessionResponseDto.builder()
                ._id(session.get_id())
                .MOM(Mommessage)
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
        if (isPhaseRequired) {
            for (Course course : courses) {
                courseResponseDtoList.add(mapCourseToResponseDto(course));
            }
        } else {
            for (Course course : courses) {
                courseResponseDtoList.add(mapCourseToResponseDtoForNoPhase(course));
            }
        }
        return courseResponseDtoList;
    }

    public static String calculateTotalEstimatedTime(List<Phase> phases) {

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
            int numOfTasksTopics = 0;
            String estimatedTime = null;
            for (CourseTask courseTask : phase.getTasks()) {
                noOfTopics += courseTask.getSubtasks().size();
                numOfTasksTopics += courseTask.getSubtasks().size();
                long totalHours = 0;
                long totalMinutes = 0;
                long phaseHours = 0;
                long phaseMinutes = 0;
                for (CourseSubTask courseSubTask : courseTask.getSubtasks()) {
                    String[] timeParts = courseSubTask.getEstimatedTime().split(":");
                    if (timeParts.length == 1) {
                        phaseHours += (timeParts[0] != null && !timeParts[0].isEmpty()) ? Long.parseLong(timeParts[0]) : 0;
                    } else if (timeParts.length == 2) {
                        phaseHours += (timeParts[0] != null && !timeParts[0].isEmpty()) ? Long.parseLong(timeParts[0]) : 0;
                        phaseMinutes += (timeParts[1] != null && !timeParts[1].isEmpty()) ? Long.parseLong(timeParts[1]) : 0;
                    }
                }
                totalHours += phaseHours + phaseMinutes / 60;
                totalMinutes += phaseMinutes % 60;
                estimatedTime = String.format("%02d:%02d", totalHours, totalMinutes);
                System.out.println("Estimated time = " + estimatedTime);
            }
            phase.setNoOfTasks(numOfTasksTopics);
            phase.setEstimatedTime(estimatedTime);
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
            int numOfTasksTopics = 0;
            String estimatedTime = null;
            for (CourseTask courseTask : phase.getTasks()) {
                noOfTopics += courseTask.getSubtasks().size();
                numOfTasksTopics += courseTask.getSubtasks().size();
                long totalHours = 0;
                long totalMinutes = 0;
                long phaseHours = 0;
                long phaseMinutes = 0;
                for (CourseSubTask courseSubTask : courseTask.getSubtasks()) {
                    String[] timeParts = courseSubTask.getEstimatedTime().split(":");
                    if (timeParts.length == 1) {
                        phaseHours += (timeParts[0] != null && !timeParts[0].isEmpty()) ? Long.parseLong(timeParts[0]) : 0;
                    } else if (timeParts.length == 2) {
                        phaseHours += (timeParts[0] != null && !timeParts[0].isEmpty()) ? Long.parseLong(timeParts[0]) : 0;
                        phaseMinutes += (timeParts[1] != null && !timeParts[1].isEmpty()) ? Long.parseLong(timeParts[1]) : 0;
                    }
                }
                totalHours += phaseHours + phaseMinutes / 60;
                totalMinutes += phaseMinutes % 60;
                estimatedTime = String.format("%02d:%02d", totalHours, totalMinutes);
                System.out.println("Estimated time = " + estimatedTime);
            }
            phase.setNoOfTasks(numOfTasksTopics);
            phase.setEstimatedTime(estimatedTime);
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

    public static String calculateTotalEstimatedTimeInTest(List<Milestone> milestones) {

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
            int numOfTopics = 0;
            String estimatedTime = null;
            for (TestTask testTask : milestone.getTasks()) {
                noOfTopics += testTask.getSubtasks().size();
                numOfTopics += testTask.getSubtasks().size();
                long totalHours = 0;
                long totalMinutes = 0;
                long phaseHours = 0;
                long phaseMinutes = 0;
                for (TestSubTask testSubTask : testTask.getSubtasks()) {
                    String[] timeParts = testSubTask.getEstimatedTime().split(":");
                    if (timeParts.length == 1) {
                        phaseHours += (timeParts[0] != null && !timeParts[0].isEmpty()) ? Long.parseLong(timeParts[0]) : 0;
                    } else if (timeParts.length == 2) {
                        phaseHours += (timeParts[0] != null && !timeParts[0].isEmpty()) ? Long.parseLong(timeParts[0]) : 0;
                        phaseMinutes += (timeParts[1] != null && !timeParts[1].isEmpty()) ? Long.parseLong(timeParts[1]) : 0;
                    }
                }
                totalHours += phaseHours + phaseMinutes / 60;
                totalMinutes += phaseMinutes % 60;
                estimatedTime = String.format("%02d:%02d", totalHours, totalMinutes);
            }
            milestone.setEstimatedTime(estimatedTime);
            milestone.setNoOfTasks(numOfTopics);
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

    public static String calculateTotalEstimatedTimeInPlan(List<com.chicmic.trainingModule.Entity.Plan.Phase> phases) {

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
        for (com.chicmic.trainingModule.Entity.Plan.Phase phase : plan.getPhases()) {
            noOfTasks += phase.getTasks().size();
        }

        List<com.chicmic.trainingModule.Entity.Plan.Phase> phaseList = new ArrayList<>();
        for (com.chicmic.trainingModule.Entity.Plan.Phase phase : plan.getPhases()) {
            com.chicmic.trainingModule.Entity.Plan.Phase newPhase = new com.chicmic.trainingModule.Entity.Plan.Phase();
            List<Task> taskList = new ArrayList<>();
            long phaseHours = 0;
            long phaseMinutes = 0;
            long totalHours = 0;
            long totalMinutes = 0;
            for (Task task : phase.getTasks()) {
                Task newTask = new Task();
                newTask.setPlanType(task.getPlanType());
                newTask.setPlan(task.getPlan());
                newTask.setIsCompleted(task.getIsCompleted());
                newTask.setEstimatedTime(task.getEstimatedTime());
                newTask.set_id(task.get_id());
                Object milestone = null;
                if (task.getPlanType() != null && task.getPlanType() == 1) {
                    System.out.println("Milestone: fet " + task.getMilestones());
                    milestone = (courseService.getCourseByPhaseIds((String) task.getPlan(), (List<Object>) task.getMilestones()));
                } else if (task.getPlanType() != null && task.getPlanType() == 2) {
                    milestone = (testService.getTestByMilestoneIds((String) task.getPlan(), (List<Object>) task.getMilestones()));
                }
                System.out.println("Milestone : " + milestone);
                String[] timeParts = task.getEstimatedTime().split(":");
                if (timeParts.length == 1) {
                    phaseHours += (timeParts[0] != null && !timeParts[0].isEmpty()) ? Long.parseLong(timeParts[0]) : 0;
                } else if (timeParts.length == 2) {
                    phaseHours += (timeParts[0] != null && !timeParts[0].isEmpty()) ? Long.parseLong(timeParts[0]) : 0;
                    phaseMinutes += (timeParts[1] != null && !timeParts[1].isEmpty()) ? Long.parseLong(timeParts[1]) : 0;
                }
                System.out.println("\u001B[31m milestone " + milestone + "\u001B[0m");
                newTask.setMilestones(milestone);
                newTask.setMentor(task.getMentor());
                taskList.add(newTask);
            }
            totalHours += phaseHours + phaseMinutes / 60;
            totalMinutes += phaseMinutes % 60;
            newPhase.set_id(phase.get_id());
            newPhase.setPhaseName(phase.getPhaseName());
            newPhase.setEstimatedTime(String.format("%02d:%02d", totalHours, totalMinutes));
            newPhase.setNoOfTasks(phase.getTasks().size());
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

    public List<AssignTaskResponseDto> mapAssignTaskToResponseDto(List<AssignTask> assignTasks, String traineeId, Principal principal) {
        List<AssignTaskResponseDto> assignTaskResponseDtoList = new ArrayList<>();
        for (AssignTask assignTask : assignTasks) {
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

    public AssignTaskResponseDto mapAssignTaskToResponseDto(AssignTask assignTask, String traineeId, Principal principal) {
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
            for (com.chicmic.trainingModule.Entity.Plan.Phase phase : plan.getPhases()) {
                for (Task task : phase.getTasks()) {
                    PlanDto planDto = null;
                    if (task.getPlanType() == 1) {
                        Course course = courseService.getCourseById(((AssignTaskPlanTrack) task.getPlan()).get_id());
                        List<MilestoneDto> milestoneDtos = new ArrayList<>();
                        List<AssignTaskPlanTrack> milestonesData = (List<AssignTaskPlanTrack>)task.getMilestones();
                        for(AssignTaskPlanTrack milestoneTrack : milestonesData) {
                            for (Phase coursePhase : course.getPhases()) {
                                if(milestoneTrack!=null && milestoneTrack.get_id().equals(coursePhase.get_id())){
                                boolean isMilestoneCompleted = false;
                                for (AssignTaskPlanTrack assignTaskPlanTrack : (List<AssignTaskPlanTrack>) task.getMilestones()) {
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

                                        for (AssignTaskPlanTrack milestonePlan : (List<AssignTaskPlanTrack>) task.getMilestones()) {
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
                                        .reviewers(task.getMentor())
                                        .name(coursePhase.getName())
                                        .feedbackId(feedbackService.getFeedbackIdForMileStoneAndPhase(String.valueOf(task.getPlanType()), ((AssignTaskPlanTrack) task.getPlan()).get_id(), coursePhase.get_id(), principal.getName(), assignTask.getUserId()))
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
                                .planType(task.getPlanType())
                                .assignPlanId(plan.get_id())
                                .isCompleted(task.getIsCompleted())
//                                .feedbackId(feedbackService.getFeedbackIdForMileStoneAndPhase(String.valueOf(task.getPlanType()), (String)task.getPlan(), coursePhase.get_id(), principal.getName(), assignTask.getUserId()))
                                .estimatedTime(task.getEstimatedTime())
                                .noOfTopics(((List<AssignTaskPlanTrack>) task.getMilestones()).size())
                                .isApproved(course.getIsApproved())
                                .isDeleted(course.getIsDeleted())
                                .milestones(milestoneDtos)
                                .name(course.getName())
                                .rating(0.0f)
                                .reviewers(task.getMentor())

                                .build();
                    } else if (task.getPlanType() == 2) {

                        Test test = testService.getTestById(((AssignTaskPlanTrack) task.getPlan()).get_id());
                        List<MilestoneDto> milestoneDtos = new ArrayList<>();
                        List<AssignTaskPlanTrack> milestonesData = (List<AssignTaskPlanTrack>) task.getMilestones();
                        for(AssignTaskPlanTrack milestoneTrack : milestonesData) {
                            for (Milestone milestone : test.getMilestones()) {
                                if(milestoneTrack.get_id().equals(milestone.get_id())){
                                boolean isMilestoneCompleted = false;
                                for (AssignTaskPlanTrack assignTaskPlanTrack : (List<AssignTaskPlanTrack>) task.getMilestones()) {
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
                                        for (AssignTaskPlanTrack milestonePlan : (List<AssignTaskPlanTrack>) task.getMilestones()) {
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
                                        .reviewers(task.getMentor())

                                        .name(milestone.getName())
                                        .feedbackId(feedbackService.getFeedbackIdForMileStoneAndPhase(String.valueOf(task.getPlanType()), ((AssignTaskPlanTrack) task.getPlan()).get_id(), milestone.get_id(), principal.getName(), assignTask.getUserId()))
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
                                .planType(task.getPlanType())
                                .assignPlanId(plan.get_id())
                                .isCompleted(task.getIsCompleted())
//                                .feedbackId("6581c5388096904d2af3204d")
                                .estimatedTime(task.getEstimatedTime())
                                .noOfTopics(((List<AssignTaskPlanTrack>) task.getMilestones()).size())
                                .isApproved(test.getApproved())
                                .isDeleted(test.getDeleted())
                                .milestones(milestoneDtos)
                                .name(test.getTestName())
                                .rating(null)
                                .reviewers(task.getMentor())

                                .build();
                    } else if (task.getPlanType() == 4) {
                        Course course = courseService.getCourseById(((AssignTaskPlanTrack)task.getPlan()).get_id());
                        List<MilestoneDto> milestoneDtos = new ArrayList<>();
                        MilestoneDto milestoneDto = MilestoneDto.builder()
                                ._id(((AssignTaskPlanTrack) task.getPlan()).get_id())
                                .reviewers(task.getMentor())

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
                                ._id(((AssignTaskPlanTrack) task.getPlan()).get_id())
                                .planType(3)
                                .name("Ppt")
                                .assignPlanId(plan.get_id())
                                .isCompleted(phase.getIsCompleted())
                                .feedbackId("6581c5388096904d2af3204d")
                                .estimatedTime(task.getEstimatedTime())
                                .noOfTopics(((List<AssignTaskPlanTrack>) task.getMilestones()).size())
                                .isApproved(false)
                                .isDeleted(false)
                                .milestones(milestoneDtos)
                                .rating(null)
                                .reviewers(task.getMentor())

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

