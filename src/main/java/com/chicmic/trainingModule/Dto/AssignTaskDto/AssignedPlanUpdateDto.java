package com.chicmic.trainingModule.Dto.AssignTaskDto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignedPlanUpdateDto {
    private List<String> plan;
    private LocalDateTime date;
    public void setDate() {
        if (date != null) {
            date = date.plusHours(5).plusMinutes(30);
        }
    }
}
