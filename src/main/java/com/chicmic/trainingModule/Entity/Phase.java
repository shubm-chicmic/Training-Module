package com.chicmic.trainingModule.Entity;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Phase {
    private String _id = String.valueOf(new ObjectId());
    private String mainTask;
    private List<SubTask> subtasks;

    @Override
    public String toString() {
        return "Phase{" +
                "mainTask='" + mainTask + '\'' +
                ", subtasks=" + subtasks +
                '}';
    }
}
