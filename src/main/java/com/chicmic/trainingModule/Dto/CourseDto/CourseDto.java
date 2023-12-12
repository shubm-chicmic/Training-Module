package com.chicmic.trainingModule.Dto.CourseDto;

import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.chicmic.trainingModule.Entity.Phase;
import com.chicmic.trainingModule.Util.TrimNullValidator.TrimAll;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

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
    Set<UserIdAndNameDto> reviewers;
    private String figmaLink;
    private String guidelines;
    @JsonProperty("phases")
    private List<List<Phase>> phases;
    private Boolean approved = false;
    private Integer status;

    @Override
    public String toString() {
        return "CourseDto{" +
                "name='" + name + '\'' +
                ", reviewers=" + reviewers +
                ", figmaLink='" + figmaLink + '\'' +
                ", guidelines='" + guidelines + '\'' +
                ", phaseList=" + phases +
                ", approved=" + approved +
                ", status=" + status +
                '}';
    }
}
