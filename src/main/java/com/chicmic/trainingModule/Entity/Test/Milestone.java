package com.chicmic.trainingModule.Entity.Test;

import com.chicmic.trainingModule.Entity.Test.TestTask;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class Milestone {
    public static int count = 0;
    @Id
    private String _id = String.valueOf(new ObjectId());
    @Transient
    private String name;
    @Transient
    private String estimatedTime;
    @Transient
    private Integer noOfTasks;
    private List<TestTask> tasks;
    public Milestone() {
        count++;
        this.name = "Milestone " + count;
    }
}
