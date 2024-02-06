package com.chicmic.trainingModule.Entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

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
    private Integer type;
    private String planTaskId;
    private String subTaskId;
    private Integer consumedTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isDeleted;
}
