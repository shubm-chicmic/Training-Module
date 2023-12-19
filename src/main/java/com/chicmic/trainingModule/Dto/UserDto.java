package com.chicmic.trainingModule.Dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
@JsonInclude(JsonInclude.Include.NON_NULL)
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
    private String teamName;
    private String empCode;
    private String employeeFullName;
}
