package com.chicmic.trainingModule.Entity;

import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.chicmic.trainingModule.Util.ConversionUtility;
import com.chicmic.trainingModule.annotation.CascadeSave;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Document
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Test {
    @Id
    private String _id;
    private String testName;
    private List<String> teams;
    @DBRef
    @CascadeSave
    private List<Phase<Task>> milestones;
    private Set<String> approver = new HashSet<>();
    private Set<String> approvedBy = new HashSet<>();
    private String createdBy;
    private Boolean deleted = false;
    private Boolean approved = false;
    private Integer estimatedTime;
    private Integer completedTasks;
    private Integer totalTasks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    public void setMilestones(List<Phase<Task>> milestones) {
        this.milestones = milestones;
        updateTotalTasks();
        updateTotalEstimateTime();
    }
//    public Test() {
//        Milestone.count = 0;
//    }
    public List<UserIdAndNameDto> getTeamsDetails() {
        return ConversionUtility.convertToTeamIdAndName(this.teams);
    }
    public List<UserIdAndNameDto> getApproverDetails() {
        return ConversionUtility.convertToUserIdAndName(this.approver);
    }

    public List<UserIdAndNameDto> getApprovedByDetails() {
        return ConversionUtility.convertToUserIdAndName(this.approvedBy);
    }
    public void updateTotalTasks(){
        if (this.milestones != null) {
            totalTasks = this.milestones.stream()
                    .mapToInt(phase -> phase.getTotalTasks())
                    .sum();
        }
    }
    private void updateTotalEstimateTime() {
        if (milestones != null) {
            estimatedTime = milestones.stream()
                    .mapToInt(phase -> phase.getEstimatedTimeInSeconds())
                    .sum();
        }
    }
    public String getEstimatedTime() {
        int hours = estimatedTime / 3600;
        int minutes = (estimatedTime % 3600) / 60;

        return String.format("%02d:%02d", hours, minutes);
    }

}
