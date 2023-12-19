package com.chicmic.trainingModule.Dto.PlanDto;

import com.chicmic.trainingModule.Dto.UserDto;
import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.chicmic.trainingModule.Entity.Plan.Phase;
import lombok.*;

import java.util.List;
import java.util.Set;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanDto {
    private String planName;
    private String description;
    private Set<String> approver;
    private List<Phase> phases;
    private Boolean approved = false;
}
