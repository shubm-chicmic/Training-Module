package com.chicmic.trainingModule.Dto.TestDto;

import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.chicmic.trainingModule.Entity.Phase;
import com.chicmic.trainingModule.Entity.Test.Milestone;
import lombok.*;
import org.bson.types.ObjectId;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestResponseDto {
    private ObjectId _id;
    private String testName;
    private String estimatedTime;
    private int noOfMilestones;
    private int noOfTopics;
    private List<UserIdAndNameDto> teams;
    private List<Phase> milestones;
    private List<UserIdAndNameDto> reviewers;
    private List<UserIdAndNameDto> approvedBy;
    private String createdBy;
    private String createdByName;
    private Boolean deleted = false;
    private Boolean approved = false;
}

