package com.chicmic.trainingModule.Dto.TestDto;

import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.chicmic.trainingModule.Entity.Milestone;
import com.chicmic.trainingModule.Entity.Phase;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestResponseDto {
    private String _id;
    private String testName;
    private String estimatedTime;
    private int noOfMilestones;
    private int noOfTopics;
    private List<UserIdAndNameDto> teams;
    private List<Milestone> milestones;
    private List<UserIdAndNameDto> reviewers;
    private List<UserIdAndNameDto> approvedBy;
    private String createdBy;
    private String createdByName;
    private Boolean deleted = false;
    private Boolean approved = false;
}

