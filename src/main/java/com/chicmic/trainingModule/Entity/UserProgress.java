package com.chicmic.trainingModule.Entity;

import com.chicmic.trainingModule.Entity.Constants.ProgessConstants;
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
public class UserProgress {
    @Id
    private String _id;
    private String traineeId;
    private String planId;
    private String courseId;
    private Integer progressType;
    private String id;
    private Integer status = ProgessConstants.NotStarted;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String feedbackId;
}
