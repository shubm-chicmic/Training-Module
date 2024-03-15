package com.chicmic.trainingModule.Dto.CourseDto;

import com.chicmic.trainingModule.Entity.Phase;
import com.chicmic.trainingModule.Entity.Task;
import com.chicmic.trainingModule.Util.TrimNullValidator.Trim;
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
public class CourseDto {
    @JsonProperty("courseName")
    @Trim
    private String name;
    private Set<String> approver;
    @Trim
    private String figmaLink = "";
    @Trim
    private String guidelines = "";
    private List<Phase<Task>> phases;
    private Boolean approved = false;

    @Override
    public String toString() {
        return "CourseDto{" +
                "name='" + name + '\'' +
                ", approver=" + approver +
                ", figmaLink='" + figmaLink + '\'' +
                ", guidelines='" + guidelines + '\'' +
                ", phases=" + phases +
                ", approved=" + approved +
                '}';
    }
}
