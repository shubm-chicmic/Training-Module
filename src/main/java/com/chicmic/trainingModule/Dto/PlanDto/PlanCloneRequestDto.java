package com.chicmic.trainingModule.Dto.PlanDto;

import com.chicmic.trainingModule.Util.TrimNullValidator.Trim;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanCloneRequestDto {
    @Trim
    private String planId;
}
