package com.chicmic.trainingModule.Entity;

import lombok.*;
import org.bson.types.ObjectId;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubTask {
    String _id = String.valueOf(new ObjectId());
    String subTask;
    String estimatedTime;

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
