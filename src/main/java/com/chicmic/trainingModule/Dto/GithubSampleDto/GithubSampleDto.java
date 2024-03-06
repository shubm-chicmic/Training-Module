package com.chicmic.trainingModule.Dto.GithubSampleDto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GithubSampleDto {
    private String projectName;
    private String url;
    private List<String> repoCreatedBy;
    private List<String> approver;
    private List<String> teams;
    private String createdBy;
    private String comment;
    private Boolean approved = false;
}
