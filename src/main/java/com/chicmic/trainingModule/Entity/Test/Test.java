package com.chicmic.trainingModule.Entity.Test;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Document
@Getter
@Setter

@AllArgsConstructor
@Builder
public class Test {
    @Id
    private String _id;
    private String testName;
    private List<String> teams;
    private List<Milestone> milestones;
    private Set<String> reviewers = new HashSet<>();
    private Set<String> approvedBy = new HashSet<>();
    private String createdBy;
    private Boolean deleted = false;
    private Boolean approved = false;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    public Test() {
        Milestone.count = 0;
    }

}
