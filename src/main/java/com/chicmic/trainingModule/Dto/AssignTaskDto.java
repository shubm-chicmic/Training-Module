package com.chicmic.trainingModule.Dto;

import lombok.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignTaskDto {
    private String userId;
    private List<String> plan;
    private Set<String> reviewers = new HashSet<>();
    private Boolean approved = false;
}
