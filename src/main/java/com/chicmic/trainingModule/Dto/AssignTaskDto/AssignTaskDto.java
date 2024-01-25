package com.chicmic.trainingModule.Dto.AssignTaskDto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignTaskDto {
    private LocalDateTime date;
    public void setDate(LocalDateTime date) {
        if(date != null)
            this.date = date.plusHours(5).plusMinutes(30);
    }
    private List<String> users;
    private List<String> planIds;
//    private Set<String> reviewers = new HashSet<>();
    private Boolean approved = false;

    @Override
    public String toString() {
        return "AssignTaskDto{" +
                "users=" + users +
                ", plans=" + planIds +
                ", approved=" + approved +
                '}';
    }
}
