package com.chicmic.trainingModule.Dto;

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
    private String userId;
    private String id;
    private Integer status;
}
