package com.chicmic.trainingModule.Dto.AssignedPlanFeedbackResponseDto;

import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackPlanDto {
    List<FeedbackCourseDto> viva;
    List<FeedbackTestDto> test;
    List<FeedbackCourseDto> ppt;
    UserIdAndNameDto plan;
}
