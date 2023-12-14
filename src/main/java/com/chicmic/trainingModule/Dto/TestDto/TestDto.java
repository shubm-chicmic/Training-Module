package com.chicmic.trainingModule.Dto.TestDto;

import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.chicmic.trainingModule.Entity.Milestone;
import com.chicmic.trainingModule.Entity.Phase;
import com.chicmic.trainingModule.Entity.StatusConstants;
import com.chicmic.trainingModule.Util.TrimNullValidator.TrimAll;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@TrimAll
public class TestDto {
    private String testName;
    private List<String> teams;
    private List<List<Milestone>> milestones;
    private Set<String> reviewers = new HashSet<>();
    private Set<String> approvedBy = new HashSet<>();
    private Integer status = StatusConstants.PENDING;
    private Boolean approved = false;
}
