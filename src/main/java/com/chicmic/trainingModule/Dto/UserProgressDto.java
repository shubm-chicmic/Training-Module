package com.chicmic.trainingModule.Dto;

import com.chicmic.trainingModule.Entity.Constants.ProgessConstants;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProgressDto {
    private Integer progressType;
    private String planId;
    private String courseId;
    private String traineeId;
    @JsonProperty("id") // Map subTaskId to id
    private String subTaskId;
    private Integer status = ProgessConstants.NotStarted;
}
