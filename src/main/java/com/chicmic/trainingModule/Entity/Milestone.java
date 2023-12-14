package com.chicmic.trainingModule.Entity;

import lombok.*;
import org.bson.types.ObjectId;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Milestone {
    private String _id = String.valueOf(new ObjectId());
    private String mainTask;
    private List<SubTask> subtasks;
}
