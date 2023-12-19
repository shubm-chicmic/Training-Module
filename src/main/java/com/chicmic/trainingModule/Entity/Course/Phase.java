package com.chicmic.trainingModule.Entity.Course;

import com.chicmic.trainingModule.Entity.Course.CourseTask;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

import java.util.List;
@Getter
@Setter
@AllArgsConstructor
//@NoArgsConstructor
@Builder
public class Phase {
    public static int count = 0;
    @Id
    private String _id = String.valueOf(new ObjectId());
    @Transient
    private String name;
    @Transient
    private String estimatedTime;
    @Transient
    private Integer noOfTasks;
    private List<CourseTask> tasks;
    public Phase() {
        count++;
        this.name = "Phase " + count;
    }
}
