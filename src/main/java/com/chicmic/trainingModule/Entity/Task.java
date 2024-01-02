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
}
