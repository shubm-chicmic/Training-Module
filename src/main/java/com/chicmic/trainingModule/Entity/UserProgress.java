package com.chicmic.trainingModule.Entity;

import com.chicmic.trainingModule.Entity.Constants.ProgessConstants;
import com.chicmic.trainingModule.Entity.Constants.StatusConstants;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
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
    private ObjectId _id;
//    @Indexed
//    private String courseId;
//    private String phaseId;
//    private String mainTaskId;
    private String subTaskId;
    private Integer status = ProgessConstants.NotStarted;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String feedbackId;
}
