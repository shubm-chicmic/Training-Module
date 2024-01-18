package com.chicmic.trainingModule.Dto.AssignTaskDto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("startDate")
    private LocalDateTime date;
    public void setDate(LocalDateTime date) {
        if(date != null)
            this.date = date.plusHours(5).plusMinutes(30);
    }
}
