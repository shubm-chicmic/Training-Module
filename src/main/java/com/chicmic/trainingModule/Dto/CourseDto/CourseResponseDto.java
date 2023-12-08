package com.chicmic.trainingModule.Dto.CourseDto;

import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
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
public class CourseResponseDto {
    private String _id;
    private String name;
    List<UserIdAndNameDto> reviewers;
    private String figmaLink;
    private String guidelines;
    List<Phase> phases;
    private List<UserIdAndNameDto> approvedBy = new ArrayList<>();
    private String createdBy;
    private int status;
    private Boolean isDeleted = false;
    private Boolean isApproved = false;
}

