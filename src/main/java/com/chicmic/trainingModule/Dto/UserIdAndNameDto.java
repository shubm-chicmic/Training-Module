package com.chicmic.trainingModule.Dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
public class UserIdAndNameDto implements Comparable<UserIdAndNameDto> {
    private String _id;
    private String name;

    public UserIdAndNameDto(String _id, String name) {
        this._id = _id;
        this.name = name;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double overallRating;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double overallPlanRating;

    @Override
    public int compareTo(UserIdAndNameDto o) {
        return String.CASE_INSENSITIVE_ORDER.compare(_id, o._id);
    }
}
