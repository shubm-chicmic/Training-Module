package com.chicmic.trainingModule.Dto.SessionDto;

import com.chicmic.trainingModule.Entity.Constants.SessionAttendedStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionAttendDto {
    private Integer attendanceStatus = SessionAttendedStatus.PENDING;
    private String reason = null;

}
