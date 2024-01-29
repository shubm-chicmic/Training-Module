package com.chicmic.trainingModule.Dto.UserTimeDto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserTimeDto {
    @NotNull
//    private String traineeId;
    @NotNull
    private String planId;
    @NotNull
    private String planTaskId;
    @NotNull
    private String subTaskId;
    @NotNull
    private Integer consumedTime;
}