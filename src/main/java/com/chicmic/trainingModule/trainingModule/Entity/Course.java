package com.chicmic.trainingModule.trainingModule.Entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document
public class Course {
    @Id
    private String id;
    private String guidelines;
    List<Object> reviewers;
    List<Phase> phases;
}
