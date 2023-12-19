package com.chicmic.trainingModule.Dto.AssignTaskDto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    private List<String> users;
    private List<String> planIds;
    private Set<String> reviewers = new HashSet<>();
    private Boolean approved = false;

    @Override
    public String toString() {
        return "AssignTaskDto{" +
                "users=" + users +
                ", plans=" + planIds +
                ", reviewers=" + reviewers +
                ", approved=" + approved +
                '}';
    }
}
