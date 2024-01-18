package com.chicmic.trainingModule.Entity;

import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.chicmic.trainingModule.Entity.Constants.StatusConstants;
import com.chicmic.trainingModule.Util.ConversionUtility;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "session")
public class Session {
    @Id
    private String _id;
    private String title;
    private List<String> teams;
    private List<String> trainees;
    private List<String> sessionBy;
    private String location;
    private Set<String> approver = new HashSet<>();
    private Set<String> approvedBy = new HashSet<>();
    private String createdBy;
    private LocalDateTime dateTime;

    public void setDateTime(LocalDateTime dateTime) {
        if(dateTime != null)
            this.dateTime = dateTime.plusHours(5).plusMinutes(30);
    }
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
        return ConversionUtility.convertToUserIdAndName(this.trainees);
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
