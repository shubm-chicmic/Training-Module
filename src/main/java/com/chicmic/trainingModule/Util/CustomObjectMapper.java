package com.chicmic.trainingModule.Util;

import com.chicmic.trainingModule.Dto.SessionResponseDto;
import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.chicmic.trainingModule.Entity.Session;
import com.chicmic.trainingModule.Service.SessionService.SessionService;
import com.chicmic.trainingModule.TrainingModuleApplication;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
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
        return SessionResponseDto.builder()
                ._id(session.get_id())
                .MOM(session.getMOM())
                .isDeleted(session.isDeleted())
                .location(session.getLocation())
                .locationName(TrainingModuleApplication.zoneCategoryMap.get(Integer.valueOf(session.getLocation())))
                .status(session.getStatus())
                .time(DateTimeUtil.getTimeFromDate(session.getTime()))
                .date(DateTimeUtil.getDateFromDate(session.getTime()))
                .title(session.getTitle())
                .teams(teams)
                .trainees(trainees)
                .sessionBy(sessionBy)
                .createdBy(session.getCreatedBy())
                .build();
    }


}

