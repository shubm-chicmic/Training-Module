package com.chicmic.trainingModule.Dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
public class UserIdAndNameDto {
    private String _id;
    private String name;

    public UserIdAndNameDto(String _id, String name) {
        this._id = _id;
        this.name = name;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Float overallRating;
}
