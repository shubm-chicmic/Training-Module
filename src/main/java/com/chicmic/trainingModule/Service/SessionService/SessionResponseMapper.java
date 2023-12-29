package com.chicmic.trainingModule.Service.SessionService;

import com.chicmic.trainingModule.Dto.SessionDto.MomMessageResponseDto;
import com.chicmic.trainingModule.Dto.SessionDto.SessionResponseDto;
import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.chicmic.trainingModule.Entity.Session;
import com.chicmic.trainingModule.TrainingModuleApplication;
import com.chicmic.trainingModule.Util.ConversionUtility;
import com.chicmic.trainingModule.Util.DateTimeUtil;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class SessionResponseMapper {
    public static List<SessionResponseDto> mapSessionToResponseDto(List<Session> sessions) {
        List<SessionResponseDto> sessionResponseDtoList = new ArrayList<>();
        for (Session session : sessions) {
            sessionResponseDtoList.add(mapSessionToResponseDto(session));
        }
        return sessionResponseDtoList;
    }

    public static SessionResponseDto mapSessionToResponseDto(Session session) {
        MomMessageResponseDto Mommessage = null;
        if(session.getMOM() != null){
            Mommessage = MomMessageResponseDto.builder()
                    ._id(session.getMOM().get_id())
                    .message(session.getMOM().getMessage())
                    .name(TrainingModuleApplication.searchNameById(session.getMOM().get_id()))
                    .build();
        }
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
                .teams(session.getTeamsDetails())
                .trainees(session.getTraineesDetails())
                .sessionBy(session.getSessionByDetails())
                .approver(session.getApproverDetails())
                .approvedBy(session.getApprovedByDetails())
                .isApproved(session.isApproved())
                .createdBy(session.getCreatedBy())
                .build();
    }
}
