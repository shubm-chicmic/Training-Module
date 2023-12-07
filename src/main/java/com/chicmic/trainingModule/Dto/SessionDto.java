package com.chicmic.trainingModule.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SessionDto {
    private String title;
    private List<String> teams;
    private List<String> trainees;
    private List<String> sessionBy;
    private String createdBy;
    private String location;
    private List<String> approver;
    private String dateTime;
    private Boolean approved = false;
    private Integer status = 1;
}
