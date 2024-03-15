package com.chicmic.trainingModule.Dto.GithubSampleDto;

import com.chicmic.trainingModule.Util.TrimNullValidator.Trim;
import com.chicmic.trainingModule.annotation.ApproverValidation;
import com.chicmic.trainingModule.annotation.UserValidation;
import lombok.*;
import java.util.List;
import jakarta.validation.constraints.Pattern;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GithubSampleDto {
    @Trim
    private String projectName;
    @Trim
    @Pattern(regexp = "/^https?:\\/\\/(?:www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b(?:[-a-zA-Z0-9()@:%_\\+.,~#?&\\/=]*)$/", message = "Invalid URL format")
    private String url;
    @UserValidation
    private List<String> repoCreatedBy;
    @ApproverValidation
    private List<String> approver;
    private List<String> teams;
    @Trim
    private String createdBy;
    @Trim
    private String comment;
    private Boolean approved = false;
}
