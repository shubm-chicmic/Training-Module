package com.chicmic.trainingModule.Dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProgressDto {
    private Integer progressType;
    private String id;
}
