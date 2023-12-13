package com.chicmic.trainingModule.Dto;

public enum FeedbackEnum {
    EMAIL("EMAIL"),
    SFTP("SFTP"),
    FTP("FTP");
    public final String name;

    FeedbackEnum(String name) {
        this.name = name;
    }
}
