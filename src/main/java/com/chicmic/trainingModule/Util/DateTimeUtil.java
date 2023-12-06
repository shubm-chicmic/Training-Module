package com.chicmic.trainingModule.Util;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeUtil {

    public static String getTimeFromDate(String dateTimeString) {
        LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        String formattedTime = dateTime.format(DateTimeFormatter.ofPattern("hh:mm a"));
        return formattedTime.replace("AM", "Am").replace("PM", "Pm");
    }

    public static String getDateFromDate(String dateTimeString) {
        LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
}