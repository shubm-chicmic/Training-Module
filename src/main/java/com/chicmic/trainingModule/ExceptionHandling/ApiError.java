package com.chicmic.trainingModule.ExceptionHandling;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;

public class ApiError {

    private final HttpStatus status;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> messages;
    private String message;
    private final Instant timestamp;

    public ApiError(HttpStatus status, List<String> message, Instant timestamp) {
        if(message.size()>1 && message.get(0).equals(message.get(message.size()-1))) {
            message.remove(message.size() - 1);
        }
        this.status= status;
        this.messages = message;
        this.timestamp = timestamp;
    }
    public ApiError(HttpStatus status,String message,Instant timestamp){
        this.status= status;
        this.message = message;
        this.timestamp = timestamp;
    }

    public HttpStatus getStatus() {
        return this.status;
    }

    public List<String> getMessages() {
        return this.messages;
    }

    public Instant getTimestamp() {
        return this.timestamp;
    }
}
