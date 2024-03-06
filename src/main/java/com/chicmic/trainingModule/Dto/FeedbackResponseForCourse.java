package com.chicmic.trainingModule.Dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Builder
@Getter
@Setter
public class FeedbackResponseForCourse {
    private String _id;
    private String employeeFullName;
    private String reviewer;
    private String employeeCode;
    private String team;
    private String type;
    private String course;
    private String phase;
    private Double theoreticalRating;
    private Double technicalRating;
    private Double communicationRating;
    private Date createdOn;
    private Double rating;
    private String comment;
}
