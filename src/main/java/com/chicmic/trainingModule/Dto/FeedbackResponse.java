package com.chicmic.trainingModule.Dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Builder
@Getter @Setter
public class FeedbackResponse {
    private String _id;
    private String employeeFullName;
    private String reviewer;
    private String employeeCode;
    private String team;
    private String type;
    private String taskName;
    private String subTask;
    private Date createdOn;
    private Float rating;
    private String comment;
    private Integer feedbackType;
}
