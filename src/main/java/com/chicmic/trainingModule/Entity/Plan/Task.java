package com.chicmic.trainingModule.Entity.Plan;

import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.bson.types.ObjectId;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
//@JsonInclude(JsonInclude.Include.NON_NULL)
public class Task {
    private String _id = String.valueOf(new ObjectId());
    private Integer planType = null;
    @NotNull(message = "Plan Id cannot be empty")
    private Object plan;
    @NotNull(message = "Milestones cannot be Empty")
    private Object milestones;
    List<UserIdAndNameDto> mentor;
    private String estimatedTime;
    private Boolean isCompleted = false;
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
}
