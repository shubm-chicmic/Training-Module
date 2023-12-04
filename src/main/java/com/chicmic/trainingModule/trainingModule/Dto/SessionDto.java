package com.chicmic.trainingModule.trainingModule.Dto;

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
    private Long id;
    private String title;
    private String time;
    private String date;
    private String location;
    private List<Object> teams;
    private List<Object> trainings;
    private List<Object> sessionsBy;
}
