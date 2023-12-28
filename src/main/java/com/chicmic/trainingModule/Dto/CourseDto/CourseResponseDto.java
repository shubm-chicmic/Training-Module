package com.chicmic.trainingModule.Dto.CourseDto;

import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.chicmic.trainingModule.Entity.Course123.Phase;
import lombok.*;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseResponseDto {
    private ObjectId _id;
    private String courseName;
    private String estimatedTime;
    private int noOfTopics;
    private List<UserIdAndNameDto> approver;
    private String figmaLink;
    private String guidelines;
    private int totalPhases;
    private List<Phase> phases;
    private List<UserIdAndNameDto> approvedBy = new ArrayList<>();
    private String createdBy;
    private String createdByName;
    private Boolean deleted = false;
    private Boolean approved = false;
}

