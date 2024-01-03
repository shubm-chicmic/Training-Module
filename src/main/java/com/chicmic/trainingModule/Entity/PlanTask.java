package com.chicmic.trainingModule.Entity;

import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.chicmic.trainingModule.Service.CourseServices.CourseService;
import com.chicmic.trainingModule.Service.TestServices.TestService;
import com.chicmic.trainingModule.Util.ConversionUtility;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
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
//    @Transient
    private List<UserIdAndNameDto> milestoneDetails;
    private List<String> mentor;
    private Integer estimatedTime;
//    public List<UserIdAndNameDto> getMilestones(){
//        List<UserIdAndNameDto> milestonesDetails = new ArrayList<>();
//        for (String milestone : milestones) {
//
//        }
//        return milestonesDetails;
//    }
    public List<UserIdAndNameDto> getMentorDetails() {
        return ConversionUtility.convertToUserIdAndName(this.mentor);
    }
    public void setEstimatedTime(String estimatedTime) {
        int hours = 0;
        int minutes = 0;
        Integer formattedTime;
        if (estimatedTime.contains(":")) {
            String[] parts = estimatedTime.split(":");
            hours = parts.length > 1 ? Integer.parseInt(parts[0]) : 0;
            minutes = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
        } else {
            hours = Integer.parseInt(estimatedTime);
            minutes = 0;
        }
//        formattedTime = String.format("%02d:%02d", hours, minutes);
        int totalSeconds = hours * 3600 + minutes * 60;
        this.estimatedTime = totalSeconds;
    }
    public String getEstimatedTime() {
        int hours = estimatedTime / 3600;
        int minutes = (estimatedTime % 3600) / 60;

        return String.format("%02d:%02d", hours, minutes);
    }
    public Integer getEstimatedTimeInSeconds() {
        return estimatedTime;
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
