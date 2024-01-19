package com.chicmic.trainingModule.Dto.UserTimeDto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignedPlanDto {
    private String _id;
    private LocalDateTime dateTime;
    private List<PlanDto> plans;
    private PlanDto plan;
}
