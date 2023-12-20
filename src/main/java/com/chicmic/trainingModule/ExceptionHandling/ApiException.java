package com.chicmic.trainingModule.ExceptionHandling;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        System.out.println("\u001B[32m" + "asjkabjksans" + "\u001B[0m");
        this.status = status;
    }

    public HttpStatus getStatus() {
        return this.status;
    }
}