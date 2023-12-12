package com.chicmic.trainingModule.Dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private String _id;
    private String token;
    private String name;
    private String teamId;
    private String empCode;
}
