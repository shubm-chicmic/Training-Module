package com.chicmic.trainingModule.Dto.SessionDto;

import com.chicmic.trainingModule.Entity.Constants.SessionAttendedStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserIdAndSessionStatusDto {
    private String _id;
    private Integer attendanceStatus;
    private String reason = null;

    public UserIdAndSessionStatusDto(String _id, Integer attendanceStatus) {
        this._id = _id;
        this.attendanceStatus = attendanceStatus;
        if (attendanceStatus != null && attendanceStatus != SessionAttendedStatus.NOT_ATTENDED) {
            this.reason = null;
        }
    }

    public void setAttendanceStatus(Integer attendanceStatus) {
        this.attendanceStatus = attendanceStatus;
        if (attendanceStatus != null && attendanceStatus != SessionAttendedStatus.NOT_ATTENDED) {
            this.reason = null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserIdAndSessionStatusDto that = (UserIdAndSessionStatusDto) o;
        return Objects.equals(_id, that._id) &&
                Objects.equals(attendanceStatus, that.attendanceStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_id, attendanceStatus);
    }

    @Override
    public String toString() {
        return "UserIdAndSessionStatusDto{" +
                "_id='" + _id + '\'' +
                ", attendanceStatus=" + attendanceStatus +
                '}';
    }
}
