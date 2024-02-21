package com.chicmic.trainingModule.Dto.UserTimeDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskTimeDto {
    private String taskId;
    private Integer consumedTime;
    private Integer estimatedTime;
}
