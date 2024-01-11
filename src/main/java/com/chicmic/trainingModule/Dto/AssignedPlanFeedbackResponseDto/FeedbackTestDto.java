package com.chicmic.trainingModule.Dto.AssignedPlanFeedbackResponseDto;

import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackTestDto {
    private String _id;
    private UserIdAndNameDto test;
    private List<UserIdAndNameDto> milestones;
}
