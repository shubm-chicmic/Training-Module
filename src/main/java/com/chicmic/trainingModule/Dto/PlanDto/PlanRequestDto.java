package com.chicmic.trainingModule.Dto.PlanDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import lombok.Setter;

import java.util.Set;
@Getter @Setter @Builder
public class PlanRequestDto {
    @NotNull(message = "trainees can't be null")
    private Set<String> trainees;

    @NotBlank(message = "plan can't be null")
    private String planId;

    @NotNull(message = "reviewers can't be null")
    private Set<String> reviewers;
}

