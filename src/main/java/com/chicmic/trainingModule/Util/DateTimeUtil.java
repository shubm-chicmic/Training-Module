package com.chicmic.trainingModule.Util;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeUtil {

    public static String getTimeFromDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "Invalid DateTime";
        }

        String formattedTime = dateTime.format(DateTimeFormatter.ofPattern("h:mm a"));
        formattedTime = formattedTime.replaceAll("(?i)AM", "Am").replaceAll("(?i)PM", "Pm");
        return formattedTime;
    }

    public static String getDateFromDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "Invalid DateTime";
        }
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
}