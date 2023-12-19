package com.chicmic.trainingModule.Entity.Plan;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Builder @Getter @Setter
@Document
public class UserPlan {
    @Id
    private String _id;
    @NotBlank(message = "trainee can't be null")
    private String traineeId;
    @NotBlank(message = "plan can't be null")
    private String planId;
    private Date createdAt;
    private Date updatedAt;
    private String createdBy;
    @NotNull(message = "reviewer can't be null")
    private Set<String> reviewerId;
}
