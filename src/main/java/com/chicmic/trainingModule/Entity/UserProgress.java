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
    private String _id;
    private String userId;
    private Integer progressType;
    private String id;
    private Integer status = ProgessConstants.NotStarted;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String feedbackId;
}
