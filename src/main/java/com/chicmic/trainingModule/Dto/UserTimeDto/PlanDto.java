package com.chicmic.trainingModule.Dto.UserTimeDto;

import com.chicmic.trainingModule.Entity.Phase;
import com.chicmic.trainingModule.Entity.PlanTask;
import com.chicmic.trainingModule.annotation.CascadeSave;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanDto {
    private String _id;
    private String planName;
    private String description;
    private Integer estimatedTime;
    private Integer totalTasks;
    private List<Phase<PlanTask>> phases;
    private Phase<PlanTask> phase;
    private String createdBy;
    private Boolean deleted = false;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
