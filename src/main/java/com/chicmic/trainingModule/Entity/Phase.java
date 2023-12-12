package com.chicmic.trainingModule.Entity;

import lombok.*;

import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Phase {
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
