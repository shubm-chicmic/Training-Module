package com.chicmic.trainingModule.Dto.AssignedPlanFeedbackResponseDto;

import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackCourseDto {
    private String _id;
    private UserIdAndNameDto course;
    private List<UserIdAndNameDto> phases;
}
