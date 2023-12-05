package com.chicmic.trainingModule.Entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Course {
    @Id
    private String _id;
    private String guidelines;
    List<Object> reviewers;
    List<Phase> phases;
    private int status;
    private Boolean isDeleted = false;
}
