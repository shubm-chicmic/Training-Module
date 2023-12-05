package com.chicmic.trainingModule.Entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GithubSample {
    @Id
    private String _id;
    private String projectName;
    private String gitSampleUrl;
    private List<Object> createdBy;
    private List<Object> approver;
    private List<Object> Teams;
    private String comment;
    private Boolean isDeleted = false;
    private Boolean isApproved = false;
}
