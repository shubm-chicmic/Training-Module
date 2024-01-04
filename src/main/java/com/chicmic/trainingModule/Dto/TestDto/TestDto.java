package com.chicmic.trainingModule.Dto.TestDto;

import com.chicmic.trainingModule.Entity.Task;
import com.chicmic.trainingModule.Util.TrimNullValidator.TrimAll;
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
    private List<List<Task>> milestones;
    private Set<String> approver = new HashSet<>();
    private Set<String> approvedBy = new HashSet<>();
    private Boolean approved = false;
}
