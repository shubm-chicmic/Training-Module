package com.chicmic.trainingModule.Entity;

import lombok.*;

import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {
    private String name;
    private List<SubTask> subTasks;
}
