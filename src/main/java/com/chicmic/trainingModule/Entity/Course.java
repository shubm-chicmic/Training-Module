package com.chicmic.trainingModule.Entity;

import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.chicmic.trainingModule.Util.ConversionUtility;
import com.chicmic.trainingModule.annotation.CascadeSave;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
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
public class Course {
    @Id
    private String _id;
    private String name;
    private String figmaLink;
    private String guidelines;
    @DBRef
    @CascadeSave

    private List<Phase<Task>> phases;
    private Set<String> approver = new HashSet<>();
    private Set<String> approvedBy = new HashSet<>();
    private String createdBy;
    private Boolean isDeleted = false;
    private Boolean isApproved = false;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer estimatedTime;
    private Integer completedTasks;
    private Integer totalTasks;
//    public Course() {
//        Phase.count = 0;
//    }
    public List<UserIdAndNameDto> getApproverDetails() {
        return ConversionUtility.convertToUserIdAndName(this.approver);
    }

    public List<UserIdAndNameDto> getApprovedByDetails() {
        return ConversionUtility.convertToUserIdAndName(this.approvedBy);
    }
    public void setPhases(List<Phase<Task>> phases) {
        this.phases = phases;
        updateTotalTasks();
        updateTotalEstimateTime();
    }
    public void updateTotalTasks(){
        if (this.phases != null) {
            totalTasks = this.phases.stream()
                    .mapToInt(phase -> phase.getTotalTasks())
                    .sum();
        }
    }
    private void updateTotalEstimateTime() {
        if (phases != null) {
            estimatedTime = phases.stream()
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