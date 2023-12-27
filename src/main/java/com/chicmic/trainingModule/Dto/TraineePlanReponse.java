package com.chicmic.trainingModule.Dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Builder @Getter @Setter
public class TraineePlanReponse {
    private String _id;
    private String name;
    private String employeeCode;
    private String team;
    private String mentor;
    private Float rating;
    private List<UserIdAndNameDto> plan;
}
