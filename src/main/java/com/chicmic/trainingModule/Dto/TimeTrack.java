package com.chicmic.trainingModule.Dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeTrack {
    private Integer estimatedTime;
    private Integer consumedTime;
}
