package com.chicmic.trainingModule.Util;

import com.chicmic.trainingModule.Dto.CourseDto.CourseResponseDto;
import com.chicmic.trainingModule.Dto.GithubSampleDto.GithubSampleResponseDto;
import com.chicmic.trainingModule.Dto.SessionDto.MomMessageResponseDto;
import com.chicmic.trainingModule.Dto.SessionDto.SessionResponseDto;
import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.chicmic.trainingModule.Entity.Course;
import com.chicmic.trainingModule.Entity.GithubSample;
import com.chicmic.trainingModule.Entity.Session;
import com.chicmic.trainingModule.TrainingModuleApplication;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.ArrayList;
import java.util.List;
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
                    String name = TrainingModuleApplication.searchUserById(traineeId);
                    return new UserIdAndNameDto(traineeId, name);
                })
                .collect(Collectors.toList());

        List<UserIdAndNameDto> sessionBy = session.getSessionBy().stream()
                .map(sessionById -> {
                    String name = TrainingModuleApplication.searchUserById(sessionById);
                    return new UserIdAndNameDto(sessionById, name);
                })
                .collect(Collectors.toList());

        List<UserIdAndNameDto> approver = session.getApprover().stream()
                .map(approverId -> {
                    String name = TrainingModuleApplication.searchUserById(approverId);
                    return new UserIdAndNameDto(approverId, name);
                })
                .collect(Collectors.toList());

        List<MomMessageResponseDto> MommessageList = session.getMOM().stream()
                .map(momMessage -> {
                    String name = TrainingModuleApplication.searchUserById(momMessage.get_id());
                    return new MomMessageResponseDto(momMessage.get_id(), name, momMessage.getMessage());
                })
                .collect(Collectors.toList());
        List<UserIdAndNameDto> approvedBy = session.getApprovedBy().stream()
                .map(approverId -> {
                    String name = TrainingModuleApplication.searchUserById(approverId);
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
                    String name = TrainingModuleApplication.searchUserById(createdById);
                    return new UserIdAndNameDto(createdById, name);
                })
                .collect(Collectors.toList());

        List<UserIdAndNameDto> approver = githubSample.getApprover().stream()
                .map(approverId -> {
                    String name = TrainingModuleApplication.searchUserById(approverId);
                    return new UserIdAndNameDto(approverId, name);
                })
                .collect(Collectors.toList());

        List<UserIdAndNameDto> approvedBy = githubSample.getApprovedBy().stream()
                .map(approverId -> {
                    String name = TrainingModuleApplication.searchUserById(approverId);
                    return new UserIdAndNameDto(approverId, name);
                })
                .collect(Collectors.toList());

        return GithubSampleResponseDto.builder()
                ._id(githubSample.get_id())
                .url(githubSample.getUrl())
                .isDeleted(githubSample.getIsDeleted())
                .projectName(githubSample.getProjectName())
                .comment(githubSample.getComment())
                .teams(teams)
                .repoCreatedBy(createdBy)
                .approver(approver)
                .approvedBy(approvedBy)
                .isApproved(githubSample.getIsApproved())
                .createdBy(githubSample.getCreatedBy())
                .build();
    }
    public static List<CourseResponseDto> mapCourseToResponseDto(List<Course> courses) {
        List<CourseResponseDto> courseResponseDtoList = new ArrayList<>();
        for (Course course : courses) {
            courseResponseDtoList.add(mapCourseToResponseDto(course));
        }
        return courseResponseDtoList;
    }

    public static CourseResponseDto mapCourseToResponseDto(Course course) {
        List<UserIdAndNameDto> approver = course.getReviewers().stream()
                .map(approverId -> {
                    String name = TrainingModuleApplication.searchUserById(approverId);
                    return new UserIdAndNameDto(approverId, name);
                })
                .collect(Collectors.toList());
        List<UserIdAndNameDto> approvedBy = course.getApprovedBy().stream()
                .map(approverId -> {
                    String name = TrainingModuleApplication.searchUserById(approverId);
                    return new UserIdAndNameDto(approverId, name);
                })
                .collect(Collectors.toList());

        return CourseResponseDto.builder()
                ._id(course.get_id())
                .guidelines(course.getGuidelines())
                .name(course.getName())
                .figmaLink(course.getFigmaLink())
                .reviewers(approver)
                .phases(course.getPhases())
                .isDeleted(course.getIsDeleted())
                .status(course.getStatus())
                .approvedBy(approvedBy)
                .isApproved(course.getIsApproved())
                .createdBy(course.getCreatedBy())
                .build();
    }


}

