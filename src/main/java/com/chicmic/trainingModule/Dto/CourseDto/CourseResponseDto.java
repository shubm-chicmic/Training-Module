package com.chicmic.trainingModule.Dto.CourseDto;

import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.chicmic.trainingModule.Entity.Phase;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseResponseDto {
    private String _id;
    private String courseName;
    private String estimatedTime;
    private int noOfTopics;
    private List<UserIdAndNameDto> reviewers;
    private String figmaLink;
    private String guidelines;
    private List<List<Phase>> phases;
    private List<UserIdAndNameDto> approvedBy = new ArrayList<>();
    private String createdBy;
    private String createdByName;
    private int status;
    private Boolean deleted = false;
    private Boolean approved = false;
}

