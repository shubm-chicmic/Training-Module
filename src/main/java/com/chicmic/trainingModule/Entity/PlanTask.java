package com.chicmic.trainingModule.Entity;

import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.chicmic.trainingModule.Util.ConversionUtility;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
@Document
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlanTask {
    @Id
    private String _id;
    private Integer planType;
    @NotNull(message = "Plan Id cannot be empty")
    private String plan;
    @NotNull(message = "Milestones cannot be Empty")
    private List<String> milestones;
    private List<String> mentor;
    private String estimatedTime;
    public List<UserIdAndNameDto> getMentorDetails() {
        return ConversionUtility.convertToUserIdAndName(this.mentor);
    }
    public String getEstimatedTime() {
        int hours = 0;
        int minutes = 0;
        String formattedTime;
        if (this.estimatedTime.contains(":")) {
            String[] parts = this.estimatedTime.split(":");
            hours = parts.length > 1 ? Integer.parseInt(parts[0]) : 0;
            minutes = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
        } else {
            hours = Integer.parseInt(this.estimatedTime);
            minutes = 0;
        }
        formattedTime = String.format("%02d:%02d", hours, minutes);
        this.estimatedTime = formattedTime;
        return this.estimatedTime;
    }

    @Override
    public String toString() {
        return "PlanTask{" +
                "_id='" + _id + '\'' +
                ", planType=" + planType +
                ", plan='" + plan + '\'' +
                ", milestones=" + milestones +
                ", mentor=" + mentor +
                ", estimatedTime='" + estimatedTime + '\'' +
                '}';
    }
}
