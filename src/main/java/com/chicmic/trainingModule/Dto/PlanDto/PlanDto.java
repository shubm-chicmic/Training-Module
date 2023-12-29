package com.chicmic.trainingModule.Dto.PlanDto;

import com.chicmic.trainingModule.Entity.Phase;
import com.chicmic.trainingModule.Entity.PlanTask;
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
    private List<Phase<PlanTask>> phases;
    private Boolean approved = false;
}
