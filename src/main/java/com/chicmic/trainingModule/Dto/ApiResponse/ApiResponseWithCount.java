package com.chicmic.trainingModule.Dto.ApiResponse;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ApiResponseWithCount {
    private long count;
    private int statusCode;
    private String message;
    private Object data;
}
