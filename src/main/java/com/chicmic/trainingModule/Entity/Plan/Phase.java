package com.chicmic.trainingModule.Entity.Plan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;

import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@Builder
public class Phase {
    private String _id = String.valueOf(new ObjectId());
    private String name;
    private List<Task> tasks;
}
