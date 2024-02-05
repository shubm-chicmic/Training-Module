package com.chicmic.trainingModule.Util;

public class FormatTime {
    public static String formatTimeIntoHHMM(Integer timeInSeconds) {
        int hours = timeInSeconds / 3600;
        int minutes = (timeInSeconds % 3600) / 60;

        return String.format("%02d:%02d", hours, minutes);
    }
}
