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
public class SessionController {
    private final SessionService sessionService;

    @GetMapping("")
    public ApiResponse getAll(){
        List<Session> sessionList = sessionService.getAllSessions();
        return new ApiResponse(HttpStatus.OK, "Sessions retrieved", sessionList);
    }
    @GetMapping("/{sessionId}")
    public ApiResponse get(@PathVariable Long sessionId){
        Session session = sessionService.getSessionById(sessionId);
        return new ApiResponse(HttpStatus.OK, "Session retrieved successfully", session);
    }

}
