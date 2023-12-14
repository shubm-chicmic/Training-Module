package com.chicmic.trainingModule.Entity;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@Builder
public class Phase {
    public static int phaseCount = 0;
    private String _id = String.valueOf(new ObjectId());
    @Transient
    private String name;
    private List<Task> tasks;
    public Phase() {
        phaseCount++;
        this.name = "Phase " + phaseCount;
    }
}
