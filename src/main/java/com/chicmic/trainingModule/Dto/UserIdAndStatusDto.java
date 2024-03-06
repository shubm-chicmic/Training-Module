package com.chicmic.trainingModule.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserIdAndStatusDto {
    @NotBlank(message = "traineeId can't be null")
    private String traineeId;
    @NotNull(message = "status can't be null")
    private Integer status;

    public UserIdAndStatusDto(String traineeId, Integer status) {
        this.traineeId = traineeId;
        this.status = status;
    }
}
