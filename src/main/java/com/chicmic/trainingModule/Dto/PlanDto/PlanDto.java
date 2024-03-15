package com.chicmic.trainingModule.Dto.PlanDto;

import com.chicmic.trainingModule.Entity.Phase;
import com.chicmic.trainingModule.Entity.PlanTask;
import com.chicmic.trainingModule.Util.TrimNullValidator.Trim;
import com.chicmic.trainingModule.annotation.PlanDtoValidation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
//@PlanDtoValidation
public class PlanDto {
    public interface First {
    }
    public interface Second {
    }
    @Trim
    @NotNull(message = "Plan Name is required", groups = First.class)
    private String planName;
    @NotNull(message = "description is required", groups = First.class)
    @Trim
    private String description;
    @NotNull(message = "approver is required", groups = Second.class)
    private Set<String> approver;
    @Valid
    @NotNull(message = "phases is required", groups = First.class)
    private List<@Valid Phase<PlanTask>> phases;
    @NotNull(message = "approved is required", groups = Second.class)
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
