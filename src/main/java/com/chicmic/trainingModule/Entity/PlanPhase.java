package com.chicmic.trainingModule.Entity;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
@Document
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlanPhase {
    private ObjectId _id;
    private String name;
    @Transient
    private String estimatedTime;
    @Transient
    private Integer noOfTasks;
    private List<Task> tasks;


}
