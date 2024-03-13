package com.chicmic.trainingModule.Dto.PlanDto;

import com.chicmic.trainingModule.Entity.Phase;
import com.chicmic.trainingModule.Entity.PlanTask;
import com.chicmic.trainingModule.Util.TrimNullValidator.Trim;
import com.chicmic.trainingModule.annotation.PlanDtoValidation;
import jakarta.validation.Valid;
import lombok.*;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@PlanDtoValidation
public class PlanDto {
    @Trim
    private String planName;
    @Trim
    private String description;
    private Set<String> approver;
    @Valid
    private List<@Valid Phase<PlanTask>> phases;
    private Boolean approved = false;

    @Override
    public String toString() {
        return "PlanDto{" +
                "planName='" + planName + '\'' +
                ", description='" + description + '\'' +
                ", approver=" + approver +
                ", milestones=" + phases +
                ", approved=" + approved +
                '}';
    }
}
