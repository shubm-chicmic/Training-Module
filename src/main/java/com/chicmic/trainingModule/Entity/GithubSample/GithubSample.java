package com.chicmic.trainingModule.Entity.GithubSample;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Document
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GithubSample {
    @Id
    private String _id;
    private String projectName;
    private String url;
    private List<String> repoCreatedBy;
    private List<String> approver;
    private List<String> teams;
    private Set<String> approvedBy = new HashSet<String>();
    private String createdBy;
    private String comment;
    private Boolean isDeleted = false;
    private Boolean isApproved = false;
}
