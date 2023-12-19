package com.chicmic.trainingModule.Dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder @Getter @Setter
public class TraineePlanReponse {
    private String _id;
    private String name;
    private UserIdAndNameDto team;
    private String mentor;
    private Float overallRating;
    private UserIdAndNameDto course;
}
