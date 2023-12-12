package com.chicmic.trainingModule.Entity;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import lombok.*;
import org.bson.types.ObjectId;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubTask {
    private String _id = String.valueOf(new ObjectId());
    private String subTask;
    private String estimatedTime;
    private String link;
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
//        this.estimatedTime = LocalTime.parse(formattedTime, DateTimeFormatter.ofPattern("HH:mm")).format(DateTimeFormatter.ofPattern("HH:mm"));
    }
    @Override
    public String toString() {
        return "SubTask{" +
                "subTask='" + subTask + '\'' +
                ", estimatedTime='" + estimatedTime + '\'' +
                ", link='" + link + '\'' +
                '}';
    }
}
