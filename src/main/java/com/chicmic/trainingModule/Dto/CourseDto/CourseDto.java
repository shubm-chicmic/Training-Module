package com.chicmic.trainingModule.Dto.CourseDto;

import com.chicmic.trainingModule.Entity.Phase;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourseDto {
    private String name;
    List<Object> reviewers;
    private String figmaLink;
    private String guidelines;
    private List<Phase> phases;
    private String createdBy;
    private Boolean approved = false;
    private Integer status;
}
