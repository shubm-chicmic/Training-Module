package com.chicmic.trainingModule.Dto.CourseDto;

import com.chicmic.trainingModule.Entity.Task;
import com.chicmic.trainingModule.Util.TrimNullValidator.TrimAll;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@TrimAll
public class CourseDto {
    @JsonProperty("courseName")
    private String name;
    private Set<String> approver = new HashSet<>();
    private String figmaLink;
    private String guidelines;
    private List<List<Task>> phases;
    private Boolean approved = false;
}
