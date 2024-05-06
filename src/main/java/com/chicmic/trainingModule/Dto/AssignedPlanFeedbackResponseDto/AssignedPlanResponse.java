package com.chicmic.trainingModule.Dto.AssignedPlanFeedbackResponseDto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignedPlanResponse {
    List<FeedbackPlanDto> plans;
}
