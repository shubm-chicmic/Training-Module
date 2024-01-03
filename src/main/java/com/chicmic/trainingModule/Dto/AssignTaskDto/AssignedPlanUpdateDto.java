package com.chicmic.trainingModule.Dto.AssignTaskDto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignedPlanUpdateDto {
    private List<String> plan;
}
