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
    private int statusCode;
    private String message;
    private Object data;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long count;
    public ApiResponse(int statusCode,String message,Object data){
        this.statusCode = statusCode;
        this.message = message;
        this.data = data;
    }
    public ApiResponse(int statusCode, String message, Object data, HttpServletResponse response) {
        this.statusCode = statusCode;
        this.message = message;
        this.data = data;
        response.setStatus(statusCode);
    }
}


