package com.chicmic.trainingModule.Dto;

import com.chicmic.trainingModule.Entity.Constants.ProgessConstants;
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
    private String id;
    private Integer status = ProgessConstants.NotStarted;
}
