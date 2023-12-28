package com.chicmic.trainingModule.Entity;

import com.chicmic.trainingModule.annotation.CascadeSave;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Phase {
    @Id
    private ObjectId _id;
    private Integer entityType;
    private String name;
    @Transient
    private String estimatedTime;
    @Transient
    private Integer noOfTasks;
    @DBRef
    @CascadeSave
    private List<Task> tasks;

}
