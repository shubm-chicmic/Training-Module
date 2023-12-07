package com.chicmic.trainingModule.Util;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeUtil {

    public static String getTimeFromDate(String dateTimeString) {
        if (dateTimeString == null) {
            return "Invalid DateTime";
        }

        LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        String formattedTime = dateTime.format(DateTimeFormatter.ofPattern("h:mm a"));
        formattedTime = formattedTime.replaceAll("(?i)AM", "Am").replaceAll("(?i)PM", "Pm");
        return formattedTime;
    }
    public static String getDateFromDate(String dateTimeString) {
        if (dateTimeString == null) {
            return "Invalid DateTime";
        }
        LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
}