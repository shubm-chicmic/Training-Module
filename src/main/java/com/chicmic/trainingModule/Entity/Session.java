package com.chicmic.trainingModule.Entity;

import com.chicmic.trainingModule.Dto.SessionDto.UserIdAndSessionStatusDto;
import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.chicmic.trainingModule.Entity.Constants.StatusConstants;
import com.chicmic.trainingModule.Util.ConversionUtility;
import com.chicmic.trainingModule.Util.TrimNullValidator.Trim;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "session")
@Builder
public class Session {
    @Id
    private String _id;
    @Trim
    private String title;
    private List<String> teams;
    private Set<UserIdAndSessionStatusDto> trainees;
    private List<String> sessionBy;
    @Trim
    private String location;
    private Set<String> approver = new HashSet<>();
    private Set<String> approvedBy = new HashSet<>();
    private String createdBy;
    private Instant dateTime;

    //    public void setDateTime(LocalDateTime dateTime) {
//        if(dateTime != null)
//            this.dateTime = dateTime.plusHours(5).plusMinutes(30);
//    }
    private int status = StatusConstants.PENDING;
    private boolean isDeleted = false;
    private boolean isApproved = false;
    private MomMessage MOM;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public List<UserIdAndNameDto> getTeamsDetails() {
        return ConversionUtility.convertToTeamIdAndName(this.teams);
    }

    public List<UserIdAndNameDto> getTraineesDetails() {
        List<String> trainees = new ArrayList<>();
        for (UserIdAndSessionStatusDto userIdAndSessionStatusDto : this.trainees) {
            trainees.add(userIdAndSessionStatusDto.get_id());
        }
        return ConversionUtility.convertToUserIdAndName(trainees);
    }

    public List<String> getTrainees() {
        List<String> trainees = new ArrayList<>();
        for (UserIdAndSessionStatusDto userIdAndSessionStatusDto : this.trainees) {
            trainees.add(userIdAndSessionStatusDto.get_id());
        }
        return trainees;
    }

    public Set<UserIdAndSessionStatusDto> getTraineesDetailsWithStatus() {
        return this.trainees;
    }

    public List<UserIdAndNameDto> getSessionByDetails() {
        return ConversionUtility.convertToUserIdAndName(this.sessionBy);
    }

    public List<UserIdAndNameDto> getApproverDetails() {
        return ConversionUtility.convertToUserIdAndName(this.approver);
    }

    public List<UserIdAndNameDto> getApprovedByDetails() {
        return ConversionUtility.convertToUserIdAndName(this.approvedBy);
    }

}
