package com.chicmic.trainingModule.Entity.Course;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CourseTask {
    private String _id = String.valueOf(new ObjectId());
    private String mainTask;
    @DBRef
    private List<CourseSubTask> subtasks;

}
