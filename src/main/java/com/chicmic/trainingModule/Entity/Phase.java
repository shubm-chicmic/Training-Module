package com.chicmic.trainingModule.Entity;

import com.chicmic.trainingModule.annotation.CascadeSave;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.models.security.SecurityScheme;
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
public class Phase<T> {
    @Id
    private String _id;
    private Integer entityType;
    private String name;
    private Integer estimatedTime;
    private Integer completedTasks;
    private Integer totalTasks;
    @DBRef
    @CascadeSave
    private List<T> tasks;
    @DBRef
    @JsonIgnore
    private Object entity;
    public void setTasks(List<T> tasks) {
        this.tasks = tasks;
        updateTotalSubTasks();
        updateTotalEstimateTime();
    }

    private void updateTotalSubTasks() {
        if (tasks != null) {
            totalTasks = tasks.stream()
                    .mapToInt(task -> {
                        if (task instanceof Task) {
                            return ((Task) task).getSubtasks().size();
                        }
//                        } else if (task instanceof PlanTask) {
//                            return ((PlanTask) task).ge;
//                        }
                        return 0;
                    })
                    .sum();
        }
    }

    private void updateTotalEstimateTime() {
        if (tasks != null) {
            estimatedTime = tasks.stream()
                    .mapToInt(task -> {
                        if (task instanceof Task) {
                            return ((Task) task).getEstimatedTimeInSeconds();
                        }
//                        else if (task instanceof PlanTask) {
//                            return ((PlanTask) task).getEstimatedTimeInSeconds();
//                        }
                        return 0;
                    })
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
