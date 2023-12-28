package com.chicmic.trainingModule.Entity;

import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.chicmic.trainingModule.TrainingModuleApplication;
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
    private ObjectId _id;
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
    public List<UserIdAndNameDto> getTeamMembers() {
        return this.teams.stream()
                .map(createdById -> {
                    String name = TrainingModuleApplication.searchTeamById(createdById);
                    return new UserIdAndNameDto(createdById, name);
                })
                .collect(Collectors.toList());
    }
    public List<UserIdAndNameDto> getApproverDetails() {
        return Optional.ofNullable(this.approver)
                .map(approverIds -> approverIds.stream()
                        .map(approverId -> {
                            String name = TrainingModuleApplication.searchNameById(approverId);
                            return new UserIdAndNameDto(approverId, name);
                        })
                        .collect(Collectors.toList())
                )
                .orElse(null);
    }
    public List<UserIdAndNameDto> getApprovedByDetails() {
        return Optional.ofNullable(this.approvedBy)
                .map(ids -> ids.stream()
                        .map(id -> {
                            String name = TrainingModuleApplication.searchNameById(id);
                            return new UserIdAndNameDto(id, name);
                        })
                        .collect(Collectors.toList())
                )
                .orElse(null);
    }

}
