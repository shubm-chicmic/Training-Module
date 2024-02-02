package com.chicmic.trainingModule.Util;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateTimeUtil {

    public static String getTimeFromDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "Invalid DateTime";
        }

        String formattedTime = dateTime.format(DateTimeFormatter.ofPattern("h:mm a"));
        formattedTime = formattedTime.replaceAll("(?i)AM", "Am").replaceAll("(?i)PM", "Pm");
        return formattedTime;
    }
    public static String getTimeFromInstant(Instant instant) {
        if (instant == null) {
            return "Invalid Instant";
        }

        ZonedDateTime zonedDateTime = instant.atZone(ZoneId.of("UTC"));
        String formattedTime = zonedDateTime.toLocalTime().format(DateTimeFormatter.ofPattern("h:mm a"));
        formattedTime = formattedTime.replaceAll("(?i)AM", "Am").replaceAll("(?i)PM", "Pm");
        return formattedTime;
    }
    public static Date convertLocalDateTimeToDate(LocalDateTime localDateTime){
        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime zonedDateTime = localDateTime.atZone(zoneId);
        Date date = Date.from(zonedDateTime.toInstant());
        return date;
    }
    public static String getDateFromDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "Invalid DateTime";
        }
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
    public static String getDateFromInstant(Instant instant) {
        if (instant == null) {
            return "Invalid Instant";
        }
        ZonedDateTime zonedDateTime = instant.atZone(ZoneId.of("UTC"));
        return zonedDateTime.toLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    public static String convertSecondsToString(Integer estimatedTime) {
        int hours = estimatedTime / 3600;
        int minutes = (estimatedTime % 3600) / 60;

        return String.format("%02d:%02d", hours, minutes);
    }
}