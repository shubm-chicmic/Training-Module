package com.chicmic.trainingModule.Dto.SessionDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SessionDto {
    private String title;
    private List<String> teams;
    private List<String> trainees;
    private List<String> sessionBy;
    private String createdBy;
    private String location;
    private List<String> approver;
    private LocalDateTime dateTime;
    private Boolean approved;
    private Integer status;
    private String message;
//    public void setDateTime(String dateTime) {
//        // Parse the dateTime string to LocalDateTime
//        LocalDateTime originalDateTime = LocalDateTime.parse(dateTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
//
//        // Add 5 hours and 30 minutes to the date and time
//        LocalDateTime adjustedDateTime = originalDateTime.plusHours(5).plusMinutes(30);
//
//        // Format the adjustedDateTime back to string
//        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
//        this.dateTime = adjustedDateTime.format(formatter);
//    }
}
