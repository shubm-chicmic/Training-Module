package com.chicmic.trainingModule.Util;

import com.chicmic.trainingModule.Dto.CourseDto.CourseResponseDto;
import com.chicmic.trainingModule.Dto.GithubSampleDto.GithubSampleResponseDto;
import com.chicmic.trainingModule.Dto.SessionDto.MomMessageResponseDto;
import com.chicmic.trainingModule.Dto.SessionDto.SessionResponseDto;
import com.chicmic.trainingModule.Dto.TestDto.TestResponseDto;
import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.chicmic.trainingModule.Entity.*;
import com.chicmic.trainingModule.TrainingModuleApplication;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class CustomObjectMapper {
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
                    String name = TrainingModuleApplication.searchTeamById(traineeId);
                    return new UserIdAndNameDto(traineeId, name);
                })
                .collect(Collectors.toList());

        List<UserIdAndNameDto> sessionBy = session.getSessionBy().stream()
                .map(sessionById -> {
                    String name = TrainingModuleApplication.searchTeamById(sessionById);
                    return new UserIdAndNameDto(sessionById, name);
                })
                .collect(Collectors.toList());

        List<UserIdAndNameDto> approver = session.getApprover().stream()
                .map(approverId -> {
                    String name = TrainingModuleApplication.searchTeamById(approverId);
                    return new UserIdAndNameDto(approverId, name);
                })
                .collect(Collectors.toList());

        List<MomMessageResponseDto> MommessageList = session.getMOM().stream()
                .map(momMessage -> {
                    String name = TrainingModuleApplication.searchTeamById(momMessage.get_id());
                    return new MomMessageResponseDto(momMessage.get_id(), name, momMessage.getMessage());
                })
                .collect(Collectors.toList());
        List<UserIdAndNameDto> approvedBy = session.getApprovedBy().stream()
                .map(approverId -> {
                    String name = TrainingModuleApplication.searchTeamById(approverId);
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

                for (Task task : phase.getTasks()) {
                    for (SubTask subTask : task.getSubtasks()) {

//                        System.out.println("Estimated time : " + subTask.getEstimatedTime());
                        String[] timeParts = subTask.getEstimatedTime().split(":");
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
            for(Task task : phase.getTasks()) {
                noOfTopics += task.getSubtasks().size();
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
            for(Task task : phase.getTasks()) {
                noOfTopics += task.getSubtasks().size();
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
    public static List<TestResponseDto> mapTestToResponseDto(List<Test> tests, Boolean isMilestoneRequired) {
        List<TestResponseDto> testResponseDtoList = new ArrayList<>();
        for (Test test : tests) {
                testResponseDtoList.add(mapTestToResponseDto(test));
        }
        return testResponseDtoList;
    }

    public static TestResponseDto mapTestToResponseDto(Test test) {
        List<UserIdAndNameDto> approver = test.getReviewers().stream()
                .map(approverId -> {
                    String name = TrainingModuleApplication.searchNameById(approverId);
                    return new UserIdAndNameDto(approverId, name);
                })
                .collect(Collectors.toList());
        List<UserIdAndNameDto> approvedBy = test.getReviewers().stream()
                .map(approverId -> {
                    String name = TrainingModuleApplication.searchNameById(approverId);
                    return new UserIdAndNameDto(approverId, name);
                })
                .collect(Collectors.toList());
        String totalEstimatedTime = calculateTotalEstimatedTime(null);
        int noOfTopics = 0;
        for (List<Milestone> milestone : test.getMilestones()) {
            noOfTopics += milestone.get(0).getSubtasks().size();
        }
        return TestResponseDto.builder()
                ._id(test.get_id())
                .testName(test.getTestName())
                .estimatedTime(totalEstimatedTime)
                .noOfTopics(noOfTopics)
                .reviewers(approver)
//                .milestones(test.getMilestones())
                .deleted(test.getDeleted())
                .approvedBy(approvedBy)
                .approved(test.getApproved())
                .createdBy(test.getCreatedBy())
                .createdByName(TrainingModuleApplication.searchNameById(test.getCreatedBy()))
                .status(test.getStatus())
                .createdByName(TrainingModuleApplication.searchNameById(test.getCreatedBy()))
                .build();
    }

}

