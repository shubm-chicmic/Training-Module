package com.chicmic.trainingModule.Entity.Plan;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Transient;

import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Phase {
    private String _id = String.valueOf(new ObjectId());
    private Boolean isCompleted = false;

    private String phaseName;
    @Transient
    private String estimatedTime;
    @Transient
    private Integer noOfTasks;
    private List<Task> tasks;


}
