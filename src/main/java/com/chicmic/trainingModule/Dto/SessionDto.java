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
    private List<Object> teams;
    private List<Object> trainees;
    private List<Object> sessionBy;
    private String location;
    private List<Object> approver;
    private String time;
}
