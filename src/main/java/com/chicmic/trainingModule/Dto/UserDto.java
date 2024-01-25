package com.chicmic.trainingModule.Dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

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
    private List<String> teams;
    private String teamName;
    private String empCode;
    private String employeeFullName;

    @Override
    public String toString() {
        return "UserDto{" +
                "_id='" + _id + '\'' +
                ", token='" + token + '\'' +
                ", name='" + name + '\'' +
                ", teamId='" + teams + '\'' +
                ", teamName='" + teamName + '\'' +
                ", empCode='" + empCode + '\'' +
                ", employeeFullName='" + employeeFullName + '\'' +
                '}';
    }
}
