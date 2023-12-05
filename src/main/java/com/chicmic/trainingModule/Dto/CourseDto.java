package com.chicmic.trainingModule.Dto;

import com.chicmic.trainingModule.Entity.Phase;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;

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
    List<Phase> phases;
}
