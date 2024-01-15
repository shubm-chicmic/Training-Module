package com.chicmic.trainingModule.Entity;

import com.chicmic.trainingModule.annotation.CascadeSave;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.stream.Collectors;

@Document
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {
    @Id
    private String _id;
    private Integer entityType;
    private String mainTask;
    private Integer estimatedTime;
    @DBRef
    @JsonIgnore
    private Phase phase;
    @DBRef
    @CascadeSave
    private List<SubTask> subtasks;
    private Boolean isDeleted = false;
    public List<SubTask> getSubtasks() {
        if (subtasks == null) {
            return null;
        }
        return subtasks.stream()
                .filter(subTask -> !subTask.getIsDeleted())
                .collect(Collectors.toList());
    }

    public void setSubtasks(List<SubTask> subtasks) {
        this.subtasks = subtasks;
        updateTotalEstimateTime();
    }

    private void updateTotalEstimateTime() {
        if (subtasks != null) {
            estimatedTime = subtasks.stream()
                    .mapToInt(subTask -> subTask.getEstimatedTimeInSeconds())
                    .sum();
        }
    }
    public Integer getEstimatedTimeInSeconds() {
        return estimatedTime;
    }
    public String getEstimatedTime() {
        int hours = estimatedTime / 3600;
        int minutes = (estimatedTime % 3600) / 60;

        return String.format("%02d:%02d", hours, minutes);
    }
    public void setEstimatedTimeInSeconds(Integer estimatedTime){
        this.estimatedTime = estimatedTime;
    }
    public void setEstimatedTime(String estimatedTime) {
        System.out.println("estimatedTime = " + estimatedTime);
        estimatedTime = estimatedTime.trim();
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

    @Override
    public String toString() {
        return "Task{" +
                "_id='" + _id + '\'' +
                ", entityType=" + entityType +
                ", mainTask='" + mainTask + '\'' +
                ", estimatedTime=" + estimatedTime +
                ", subtasks=" + subtasks +
                ", isDeleted=" + isDeleted +
                '}';
    }
}
