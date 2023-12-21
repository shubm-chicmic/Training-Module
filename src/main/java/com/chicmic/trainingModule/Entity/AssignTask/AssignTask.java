package com.chicmic.trainingModule.Entity.AssignTask;

import com.chicmic.trainingModule.Entity.Plan.Plan;
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
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignTask {
    @Id
    private String _id;
    private String userId;
    private LocalDateTime date;
    private List<Plan> plans;
    private Set<String> reviewers = new HashSet<>();
    private Set<String> approvedBy = new HashSet<>();
    private String createdBy;
    private Boolean deleted = false;
    private Boolean approved = false;
}
