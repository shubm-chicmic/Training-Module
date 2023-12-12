package com.chicmic.trainingModule.Entity;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Milestone {
    private String mainTask;
    private List<SubTask> subtasks;
}
