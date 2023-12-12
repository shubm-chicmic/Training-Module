package com.chicmic.trainingModule.Dto.GithubSampleDto;

import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import lombok.*;

import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GithubSampleResponseDto {
    private String _id;
    private String projectName;
    private String url;
    private List<UserIdAndNameDto> repoCreatedBy;
    private List<UserIdAndNameDto> approver;
    private List<UserIdAndNameDto> teams;
    private List<UserIdAndNameDto> approvedBy;
    private String comment;
    private String createdBy;
    private Boolean deleted = false;
    private Boolean approved = false;
}
