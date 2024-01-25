package com.chicmic.trainingModule.Dto.AssignTaskDto;

import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanTaskResponseDto {
    private String _id;
    private String phaseName;
    private Integer planType;
    private UserIdAndNameDto plan;
    private Integer completedTasks;
    private Integer totalTasks;
    private List<UserIdAndNameDto> phases;
    private List<UserIdAndNameDto> mentor;
    private String estimatedTime;
    private String consumedTime;
    private LocalDateTime date;
    private Boolean isCompleted;
    private String feedbackId;
    private double rating;
}
