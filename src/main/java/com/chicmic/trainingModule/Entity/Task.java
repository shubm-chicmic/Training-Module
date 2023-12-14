package com.chicmic.trainingModule.Entity;

import lombok.*;
import org.bson.types.ObjectId;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Task {
    private String _id = String.valueOf(new ObjectId());
    private String mainTask;
    private List<SubTask> subtasks;

}
