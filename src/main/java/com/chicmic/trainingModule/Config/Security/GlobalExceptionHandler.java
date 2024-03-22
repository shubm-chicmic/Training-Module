package com.chicmic.trainingModule.Config.Security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = {AccessDeniedException.class})
    public ResponseEntity<Object> handleAccessDeniedException(HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex) throws IOException {
        // Create the error response structure
        Map<String, Object> errorResponse = new LinkedHashMap<>();
        errorResponse.put("timestamp", new Date());
        errorResponse.put("status", HttpStatus.FORBIDDEN.value());
        errorResponse.put("error", HttpStatus.FORBIDDEN.getReasonPhrase());
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("path", request.getRequestURI());

        // Return the error response
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    // Add more exception handlers as needed
}
