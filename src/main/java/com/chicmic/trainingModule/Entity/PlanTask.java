package com.chicmic.trainingModule.Entity;

import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.chicmic.trainingModule.Service.CourseServices.CourseService;
import com.chicmic.trainingModule.Service.TestServices.TestService;
import com.chicmic.trainingModule.Util.ConversionUtility;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
@Document
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanTask {
    @Id
    private String _id;
    private Integer planType;
    @NotNull(message = "Plan Id cannot be empty")
    private String plan;
    @Transient
    private String planName;
    @NotNull(message = "Milestones cannot be Empty")
    private List<Object> milestones= new ArrayList<>();
//    @Transient
//    private List<UserIdAndNameDto> milestoneDetails;
    private List<String> mentor;
    private Integer totalTasks;
    private Instant date;
//    public void setDatePlusHours(LocalDateTime date) {
//        if(date != null)
//        this.date = date.plusHours(5).plusMinutes(30);
//    }
    private Integer estimatedTime;
    private Boolean isDeleted = false;
    @DBRef
    @JsonIgnore
    private Phase<PlanTask> phase;
    @DBRef
    @JsonIgnore
    private List<Plan> plans = new ArrayList<>();
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
    public List<UserIdAndNameDto> getMentor() {
        return ConversionUtility.convertToUserIdAndName(this.mentor);
    }
    public List<String> getMentorIds() {
        return this.mentor;
    }

    public void setEstimatedTime(String estimatedTime) {
        estimatedTime = estimatedTime.trim();
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
    public String getMilestonesEstimatedTime() {
        int hours = estimatedTime / 3600;
        int minutes = (estimatedTime % 3600) / 60;

        return String.format("%02d:%02d", hours, minutes);
    }
    public Integer getEstimatedTimeInSeconds() {
        return estimatedTime;
    }
    public void setEstimatedTimeInSeconds(Integer estimatedTimeInSeconds) {
        this.estimatedTime = estimatedTimeInSeconds;
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
