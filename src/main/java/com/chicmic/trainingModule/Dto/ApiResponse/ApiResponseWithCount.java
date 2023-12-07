package com.chicmic.trainingModule.Dto.ApiResponse;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.servlet.http.HttpServletResponse;
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
    public ApiResponseWithCount(long count,int statusCode, String message, Object data, HttpServletResponse response) {
        this.count = count;
        this.statusCode = statusCode;
        this.message = message;
        this.data = data;
        response.setStatus(statusCode);
    }
}
