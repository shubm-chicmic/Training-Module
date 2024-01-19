package com.chicmic.trainingModule.Dto.ApiResponse;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ApiResponse {
    private int status;
    private String message;
    private Object data;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long count;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double overallRating;

    public ApiResponse(int status, String message, Object data){
        this.status = status;
        this.message = message;
        this.data = data;
    }
    public ApiResponse(int status, String message, Object data, HttpServletResponse response) {
        this.status = status;
        this.message = message;
        this.data = data;
        response.setStatus(status);
    }

    public ApiResponse(int status, String message, Object data, Long count) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.count = count;
    }
}


