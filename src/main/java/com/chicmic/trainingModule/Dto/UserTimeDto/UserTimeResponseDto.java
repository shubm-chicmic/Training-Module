package com.chicmic.trainingModule.Dto.UserTimeDto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserTimeResponseDto {
    private String _id;
    private Integer totalTasks;
    private List<TaskTimeDto> tasks;
}
