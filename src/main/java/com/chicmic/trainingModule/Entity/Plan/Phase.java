package com.chicmic.trainingModule.Entity.Plan;

import lombok.*;
import org.bson.types.ObjectId;

import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Phase {
    private String _id = String.valueOf(new ObjectId());
    private String phaseName;
    private List<Task> tasks;
    private Boolean isCompleted = false;

}
