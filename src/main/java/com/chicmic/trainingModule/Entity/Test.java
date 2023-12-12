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
public class Test {
    @Id
    private String _id;
    private String testName;
    private List<String> teams;
    private List<List<Milestone>> milestones;
    private Set<String> reviewers = new HashSet<>();
    private Set<String> approvedBy = new HashSet<>();
    private String createdBy;
    private int status = StatusConstants.PENDING;
    private Boolean deleted = false;
    private Boolean approved = false;

}
