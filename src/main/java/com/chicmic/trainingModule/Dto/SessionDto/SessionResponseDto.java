package com.chicmic.trainingModule.Dto.SessionDto;

import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.chicmic.trainingModule.Entity.Constants.SessionAttendedStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.bson.types.ObjectId;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SessionResponseDto {
    private String _id;
    private String title;
    private String time;
    private String date;
    private Instant dateTime;
    private List<UserIdAndNameDto> teams;
    private List<UserIdAndNameDto> trainees;
    private List<UserIdAndNameDto> sessionBy;
    private List<UserIdAndNameDto> approver;
    private List<UserIdAndNameDto> approvedBy = new ArrayList<>();
    private String createdBy;
    private String location;
    private String locationName;
    private Integer status;
    private Boolean isDeleted = false;
    private Boolean isApproved = false;
    private Integer attendanceStatus = SessionAttendedStatus.PENDING;
    private String reason = null;
    private MomMessageResponseDto MOM;
}
