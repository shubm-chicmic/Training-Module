package com.chicmic.trainingModule.Entity;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubTask {
    @Id
    private ObjectId _id;
    private String entityType;
    private String subTask;
    private String estimatedTime;
    private String link;
    private String reference;
    public void setEstimatedTime(String estimatedTime) {
        int hours = 0;
        int minutes = 0;
        String formattedTime;
        if (estimatedTime.contains(":")) {
            String[] parts = estimatedTime.split(":");
            hours = parts.length > 1 ? Integer.parseInt(parts[0]) : 0;
            minutes = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
        } else {
            hours = Integer.parseInt(estimatedTime);
            minutes = 0;
        }
        formattedTime = String.format("%02d:%02d", hours, minutes);
        this.estimatedTime = formattedTime;
    }
}
