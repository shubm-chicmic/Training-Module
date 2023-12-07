package com.chicmic.trainingModule.Dto.ApiResponse;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.servlet.http.HttpServletResponse;
import lombok.*;
import org.springframework.http.HttpStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ApiResponse {
    private int statusCode;
    private String message;
    private Object data;

    public ApiResponse(int statusCode, String message, Object data, HttpServletResponse response) {
        this.statusCode = statusCode;
        this.message = message;
        this.data = data;
        response.setStatus(statusCode);
    }
}


