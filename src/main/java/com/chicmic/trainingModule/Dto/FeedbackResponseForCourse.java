package com.chicmic.trainingModule.Dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
@Builder @Getter @Setter
public class FeedbackResponseForCourse {
    private String _id;
    private String employeeFullName;
    private String reviewer;
    private String employeeCode;
    private String team;
    private String type;
    private String course;
    private String phase;
    private Float theoreticalRating;
    private Float technicalRating;
    private Float communicationRating;
    private Date createdOn;
    private Float rating;
    private String comment;
}
