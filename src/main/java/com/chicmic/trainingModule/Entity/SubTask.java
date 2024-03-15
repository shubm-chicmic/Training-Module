package com.chicmic.trainingModule.Entity;

import com.chicmic.trainingModule.Util.TrimNullValidator.Trim;
import com.chicmic.trainingModule.Util.TrimNullValidator.TrimAll;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubTask {
    @Id
    private String _id;
    private Integer entityType;
    @JsonProperty(value = "subTask", required = true)
    @Trim
    @NotBlank(message = "Subtask Name is Required")
    private String subTask;
    private Integer estimatedTime;
    @Trim
    @Pattern(regexp = "/^https?:\\/\\/(?:www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b(?:[-a-zA-Z0-9()@:%_\\+.,~#?&\\/=]*)$/", message = "Invalid Link format")
    private String link = "";
    @Trim
    @Pattern(regexp = "/^https?:\\/\\/(?:www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b(?:[-a-zA-Z0-9()@:%_\\+.,~#?&\\/=]*)$/", message = "Invalid Reference format")
    private String reference;
    @DBRef
    @JsonIgnore
    private Task task;
    @DBRef
    @JsonIgnore
    private Phase<Task> phase;
    private Boolean isDeleted = false;

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

    public Integer getEstimatedTimeInSeconds() {
        return estimatedTime;
    }

    @Override
    public String toString() {
        return "SubTask{" +
                "_id='" + _id + '\'' +
                ", entityType=" + entityType +
                ", subTask='" + subTask + '\'' +
                ", estimatedTime=" + estimatedTime +
                ", link='" + link + '\'' +
                ", reference='" + reference + '\'' +
                '}';
    }
}
