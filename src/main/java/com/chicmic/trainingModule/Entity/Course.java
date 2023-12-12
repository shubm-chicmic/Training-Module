package com.chicmic.trainingModule.Entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Document
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Course {
    @Id
    private String _id;
    private String name;
    private String figmaLink;
    private String guidelines;
    List<Phase> phases;
    private Set<String> reviewers = new HashSet<>();
    private Set<String> approvedBy = new HashSet<>();
    private String createdBy;
    private int status;
    private Boolean isDeleted = false;
    private Boolean isApproved = false;
}
