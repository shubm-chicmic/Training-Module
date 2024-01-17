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
    public static LocalDateTime convertStringToDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            throw new IllegalArgumentException("Invalid date string");
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return LocalDateTime.parse(dateString, formatter);
    }
    public static String convertLocalDateTimeToDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            throw new IllegalArgumentException("Invalid LocalDateTime");
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return dateTime.format(formatter);
    }

    public static String getDateFromDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "Invalid DateTime";
        }
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    public static String convertSecondsToString(Integer estimatedTime) {
        int hours = estimatedTime / 3600;
        int minutes = (estimatedTime % 3600) / 60;

        return String.format("%02d:%02d", hours, minutes);
    }
}