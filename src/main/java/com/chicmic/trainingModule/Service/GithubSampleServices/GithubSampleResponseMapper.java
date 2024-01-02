package com.chicmic.trainingModule.Service.GithubSampleServices;

import com.chicmic.trainingModule.Dto.GithubSampleDto.GithubSampleResponseDto;
import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.chicmic.trainingModule.Entity.GithubSample;
import com.chicmic.trainingModule.TrainingModuleApplication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GithubSampleResponseMapper {
    public static List<GithubSampleResponseDto> mapGithubSampleToResponseDto(List<GithubSample> githubSamples) {
        List<GithubSampleResponseDto> githubSampleResponseDtoList = new ArrayList<>();
        for (GithubSample githubSample : githubSamples) {
            githubSampleResponseDtoList.add(mapGithubSampleToResponseDto(githubSample));
        }
        return githubSampleResponseDtoList;
    }

    public static GithubSampleResponseDto mapGithubSampleToResponseDto(GithubSample githubSample) {
        return GithubSampleResponseDto.builder()
                ._id(githubSample.get_id())
                .url(githubSample.getUrl())
                .deleted(githubSample.getIsDeleted())
                .projectName(githubSample.getProjectName())
                .comment(githubSample.getComment())
                .teams(githubSample.getTeamMembers())
                .repoCreatedBy(githubSample.getRepoCreatedByDetails())
                .approver(githubSample.getApproverDetails())
                .approvedBy(githubSample.getApprovedByDetails())
                .approved(githubSample.getIsApproved())
                .createdBy(githubSample.getCreatedBy())
                .build();
    }

}
