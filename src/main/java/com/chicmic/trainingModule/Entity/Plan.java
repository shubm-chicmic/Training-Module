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
import java.util.stream.Collectors;

@Document
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plan {
    @Id
    private String _id;
    private String planName;
    private String description;
    private Integer estimatedTime;
    private Integer totalTasks;
    @DBRef
    @CascadeSave
    private List<Phase<PlanTask>> phases;
    private Set<String> approver = new HashSet<>();
    private Set<String> approvedBy = new HashSet<>();
    private String createdBy;
    private Boolean deleted = false;
    private Boolean approved = false;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    public List<Phase<PlanTask>> getPhases() {
        if (phases == null) {
            return null;
        }
        return phases.stream()
                .filter(phase -> !phase.getIsDeleted())
                .collect(Collectors.toList());
    }
    public void setPhases(List<Phase<PlanTask>> phases) {
        this.phases = phases;
        updateTotalTasks();
        updateTotalEstimateTime();
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
    public Integer getEstimatedTimeInSeconds() {
        return estimatedTime;
    }
    public void setEstimatedTimeInSeconds(Integer estimatedTime) {
        this.estimatedTime = estimatedTime;
    }
    public void setEstimatedTime(String estimatedTime) {
        int hours = 0;
        int minutes = 0;
        Integer formattedTime;
        if (estimatedTime.contains(":")) {
            String[] parts = estimatedTime.split(":");
            hours = parts.length > 1 ? Integer.parseInt(parts[0]) : 0;
            minutes = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
        } else {
            hours = Integer.parseInt(estimatedTime);
            minutes = 0;
        }
//        formattedTime = String.format("%02d:%02d", hours, minutes);
        int totalSeconds = hours * 3600 + minutes * 60;
        this.estimatedTime = totalSeconds;
    }

    public void updateTotalTasks(){
        if (this.phases != null) {
            totalTasks = this.phases.stream()
                    .mapToInt(phase -> phase.getTotalTasks())
                    .sum();
        }
    }
    public List<UserIdAndNameDto> getApproverDetails() {
        return ConversionUtility.convertToUserIdAndName(this.approver);
    }

    public List<UserIdAndNameDto> getApprovedByDetails() {
        return ConversionUtility.convertToUserIdAndName(this.approvedBy);
    }

    @Override
    public String toString() {
        return "Plan{" +
                "_id='" + _id + '\'' +
                ", planName='" + planName + '\'' +
                ", description='" + description + '\'' +
                ", estimatedTime=" + estimatedTime +
                ", totalTasks=" + totalTasks +
                ", phases=" + phases +
                ", approver=" + approver +
                ", approvedBy=" + approvedBy +
                ", createdBy='" + createdBy + '\'' +
                ", deleted=" + deleted +
                ", approved=" + approved +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
