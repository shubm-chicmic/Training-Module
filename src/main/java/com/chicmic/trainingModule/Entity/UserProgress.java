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
    private String planTaskId;
    private Integer progressType;
    private String subTaskId;
    private Integer status = ProgessConstants.NotStarted;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String feedbackId;

    @Override
    public String toString() {
        return "UserProgress{" +
                "_id='" + _id + '\'' +
                ", traineeId='" + traineeId + '\'' +
                ", planId='" + planId + '\'' +
                ", courseId='" + courseId + '\'' +
                ", progressType=" + progressType +
                ", id='" + subTaskId + '\'' +
                ", status=" + status +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", feedbackId='" + feedbackId + '\'' +
                '}';
    }
}
