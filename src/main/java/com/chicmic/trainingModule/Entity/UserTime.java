package com.chicmic.trainingModule.Entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserTime {
    @Id
    private String _id;
    private String traineeId;
    private String planId;
    private String phaseId;
    private String taskId;
    private Integer consumedTime;
    private Boolean isDeleted;
    private String createdBy;
}
