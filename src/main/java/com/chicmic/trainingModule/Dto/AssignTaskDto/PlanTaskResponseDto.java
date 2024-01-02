package com.chicmic.trainingModule.Dto.AssignTaskDto;

import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanTaskResponseDto {
    private String _id;
    private Integer planType;
    private UserIdAndNameDto plan;
    private List<String> phases;
    private List<UserIdAndNameDto> mentor;
    private String estimatedTime;
    private String consumedTime;
}
