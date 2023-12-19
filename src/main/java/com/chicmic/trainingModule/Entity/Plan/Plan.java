package com.chicmic.trainingModule.Entity.Plan;

import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
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
public class Plan {
    @Id
    private String _id;
    private String planName;
    private String description;
    private Set<String> approver;
    private List<Phase> phases;
    private Set<String> approvedBy = new HashSet<>();
    private String createdBy;
    private Boolean deleted = false;
    private Boolean approved = false;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
