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
import java.util.stream.Collectors;

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
    private Integer estimatedTime = 0;
    private Integer completedTasks = 0;
    private Integer totalTasks = 0;
    @DBRef
    @CascadeSave
    private List<T> tasks;
    @DBRef
    @JsonIgnore
    private Object entity;
    private Boolean isDeleted = false;
    public List<T> getTasks() {
        if (tasks == null) {
            return null;
        }
        return tasks.stream()
                .filter(task -> {
                    if (task instanceof Task) {
                        return !((Task) task).getIsDeleted();
                    } else if (task instanceof PlanTask) {
                        return !((PlanTask) task).getIsDeleted();
                    }
                    return false;
                })
                .collect(Collectors.toList());
    }
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

                        } else if (task instanceof PlanTask) {
                            return 1;
                        }
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
                        else if (task instanceof PlanTask) {
                            return ((PlanTask) task).getEstimatedTimeInSeconds();
                        }
                        return 0;
                    })
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
    public void setEstimatedTimeInSeconds(Integer estimatedTimeInSeconds) {
        this.estimatedTime = estimatedTimeInSeconds;
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


    @Override
    public String toString() {
        return "Phase{" +
                "_id='" + _id + '\'' +
                ", entityType=" + entityType +
                ", name='" + name + '\'' +
                ", estimatedTime=" + estimatedTime +
                ", completedTasks=" + completedTasks +
                ", totalTasks=" + totalTasks +
                ", tasks=" + tasks +
                '}';
    }
}
