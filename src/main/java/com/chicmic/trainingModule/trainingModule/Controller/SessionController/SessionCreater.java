package com.chicmic.trainingModule.trainingModule.Controller.SessionController;

import com.chicmic.trainingModule.trainingModule.Dto.ApiResponse;
import com.chicmic.trainingModule.trainingModule.Dto.SessionDto;
import com.chicmic.trainingModule.trainingModule.Entity.Session;
import com.chicmic.trainingModule.trainingModule.Service.SessionService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/session")
@AllArgsConstructor
public class SessionCreater {
    private final SessionService sessionService;
    @PostMapping("/create")
    public ApiResponse create(@RequestBody Session session) {
        session = sessionService.createSession(session);
        return new ApiResponse(HttpStatus.CREATED, "Session created successfully", session);
    }
    @DeleteMapping("/delete/{sessionId}")
    public ApiResponse delete(@PathVariable Long sessionId){
        Boolean deleted = sessionService.deleteSessionById(sessionId);
        if(deleted){
            return new ApiResponse(HttpStatus.OK, "Session deleted successfully", null);
        }
        return new ApiResponse(HttpStatus.NOT_FOUND, "Session not found", null);
    }
    @PutMapping("/update/status/{sessionId}")
    public ApiResponse updateStatus(@PathVariable Long sessionId, @RequestBody String status){
        Session session = sessionService.updateStatus(sessionId, status);
        return new ApiResponse(HttpStatus.CREATED, "Session updated successfully", session);
    }
    @PutMapping("/update")
    public ApiResponse updateSession(@RequestBody SessionDto sessionDto){
        Session updatedSession = sessionService.updateSession(sessionDto);
        return new ApiResponse(HttpStatus.CREATED, "Session updated successfully", updatedSession);
    }
    @PostMapping("/mom/{sessionId}")
    public ApiResponse postMOM(@PathVariable Long sessionId, @RequestBody String message) {
        Session session = sessionService.postMOM(sessionId, message);
        return new ApiResponse(HttpStatus.CREATED, "Session updated successfully", session);
    }
}
