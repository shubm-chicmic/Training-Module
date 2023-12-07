package com.chicmic.trainingModule.Dto;

import org.springframework.data.annotation.Id;

import java.util.List;

public class GithubSampleDto {
    private String projectName;
    private String url;
    private List<String> createdBy;
    private List<String> approver;
    private List<String> Teams;
    private String comment;
}
