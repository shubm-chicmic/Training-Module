package com.chicmic.trainingModule.Dto;

import org.springframework.data.annotation.Id;

import java.util.List;

public class GithubSampleDto {
    private String projectName;
    private String gitSampleUrl;
    private List<Object> createdBy;
    private List<Object> approver;
    private List<Object> Teams;
    private String comment;
}
