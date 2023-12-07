package com.chicmic.trainingModule.Dto;

import lombok.*;

import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionResponseDto {
    private String _id;
    private String title;
    private String time;
    private String date;
    private String dateTime;
    private List<UserIdAndNameDto> teams;
    private List<UserIdAndNameDto> trainees;
    private List<UserIdAndNameDto> sessionBy;
    private List<UserIdAndNameDto> approver;
    private String createdBy;
    private String location;
    private String locationName;
    private int status;
    private boolean isDeleted = false;
    private boolean isApproved = false;
    private List<MomMessageResponseDto> MOM;
}
