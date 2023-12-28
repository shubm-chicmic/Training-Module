package com.chicmic.trainingModule.Entity;

import com.chicmic.trainingModule.annotation.CascadeSave;
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
public class Phase {
    @Id
    private ObjectId _id;
    private Integer entityType;
    private String name;
    @Transient
    private String estimatedTime;
    @Transient
    private Integer totalSubTasks;
    @DBRef
    @CascadeSave
    private List<Task> tasks;
    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
        updateTotalSubTasks();
    }

    private void updateTotalSubTasks() {
        if (tasks != null) {
            totalSubTasks = tasks.stream()
                    .mapToInt(task -> task.getSubtasks().size())
                    .sum();
        }
    }

}
