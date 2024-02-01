package com.chicmic.trainingModule.Service.SessionService;

import com.chicmic.trainingModule.Dto.SessionDto.MomMessageResponseDto;
import com.chicmic.trainingModule.Dto.SessionDto.SessionResponseDto;
import com.chicmic.trainingModule.Dto.SessionDto.UserIdAndSessionStatusDto;
import com.chicmic.trainingModule.Entity.Constants.SessionAttendedStatus;
import com.chicmic.trainingModule.Entity.Session;
import com.chicmic.trainingModule.TrainingModuleApplication;
import com.chicmic.trainingModule.Util.DateTimeUtil;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@Service
public class SessionResponseMapper {
    public static List<SessionResponseDto> mapSessionToResponseDto(List<Session> sessions, String userId) {
        List<SessionResponseDto> sessionResponseDtoList = new ArrayList<>();
        for (Session session : sessions) {
            sessionResponseDtoList.add(mapSessionToResponseDto(session, userId));
        }
        return sessionResponseDtoList;
    }

    public static SessionResponseDto mapSessionToResponseDto(Session session, String userId) {
        MomMessageResponseDto Mommessage = null;
        if(session.getMOM() != null){
            Mommessage = MomMessageResponseDto.builder()
                    ._id(session.getMOM().get_id())
                    .message(session.getMOM().getMessage())
                    .name(TrainingModuleApplication.searchNameById(session.getMOM().get_id()))
                    .build();
        }
        Integer attendanceStatus = SessionAttendedStatus.PENDING;
        String reason = null;
        Set<UserIdAndSessionStatusDto> attendedTrainees = session.getTraineesDetailsWithStatus();
        for (UserIdAndSessionStatusDto traineeSessionStatus : attendedTrainees) {
            if(traineeSessionStatus.get_id().equals(userId)){
                attendanceStatus = traineeSessionStatus.getAttendanceStatus();
                if(traineeSessionStatus.getAttendanceStatus() == SessionAttendedStatus.NOT_ATTENDED){
                    reason = traineeSessionStatus.getReason();
                }
            }
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
                .attendanceStatus(attendanceStatus)
                .reason(reason)
                .build();
    }
}
