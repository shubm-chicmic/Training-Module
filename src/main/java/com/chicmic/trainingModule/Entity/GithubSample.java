package com.chicmic.trainingModule.Entity;

import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.chicmic.trainingModule.TrainingModuleApplication;
import com.chicmic.trainingModule.Util.ConversionUtility;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Document
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GithubSample {
    @Id
    private String _id;
    private String projectName;
    private String url;
    private List<String> repoCreatedBy;
    private List<String> teams;
    private Set<String> approver;
    private Set<String> approvedBy = new HashSet<String>();
    private String createdBy;
    private String comment;
    private Boolean isDeleted = false;
    private Boolean isApproved = false;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    public List<UserIdAndNameDto> getApproverDetails() {
        return ConversionUtility.convertToUserIdAndName(this.approver);
    }

    public List<UserIdAndNameDto> getApprovedByDetails() {
        return ConversionUtility.convertToUserIdAndName(this.approvedBy);
    }
    public List<UserIdAndNameDto> getTeamMembers() {
        return ConversionUtility.convertToTeamIdAndName(this.teams);
    }
    public List<UserIdAndNameDto> getRepoCreatedByDetails() {
        return ConversionUtility.convertToUserIdAndName(this.repoCreatedBy);
    }

}
