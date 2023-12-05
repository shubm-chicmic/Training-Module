package com.chicmic.trainingModule.Controller.SessionController;

import com.chicmic.trainingModule.Dto.*;
import com.chicmic.trainingModule.Entity.Session;
import com.chicmic.trainingModule.Service.SessionService.SessionService;
import com.chicmic.trainingModule.Util.CustomObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/training/session")
@AllArgsConstructor
public class SessionCRUD {
    private final SessionService sessionService;

    @GetMapping
    public ApiResponse getAll(
            @RequestParam(value = "index", defaultValue = "0", required = false) Integer pageNumber,
            @RequestParam(value = "limit", defaultValue = "10", required = false) Integer pageSize
    ) {
        pageNumber /= pageSize;
        if (pageNumber < 0 || pageSize < 1) return new ApiResponse(HttpStatus.NO_CONTENT.value(), "invalid pageNumber or pageSize", null);
        List<Session> sessionList = sessionService.getAllSessions(pageNumber, pageSize);
        return new ApiResponse(HttpStatus.OK.value(), "Sessions retrieved", sessionList);

    }

    @GetMapping("/{sessionId}")
    public ApiResponse get(@PathVariable Long sessionId) {
        Session session = sessionService.getSessionById(sessionId);
        return new ApiResponse(HttpStatus.OK.value(), "Session retrieved successfully", session);
    }


    @PostMapping
    public ApiResponse create(@RequestBody SessionDto sessionDto) {
        sessionDto = CustomObjectMapper.convert(sessionService.createSession(CustomObjectMapper.convert(sessionDto, Session.class)), SessionDto.class);
        return new ApiResponse(HttpStatus.CREATED.value(), "Session created successfully", sessionDto);
    }

    @DeleteMapping("/{sessionId}")
    public ApiResponse delete(@PathVariable Long sessionId) {
        Boolean deleted = sessionService.deleteSessionById(sessionId);
        if (deleted) {
            return new ApiResponse(HttpStatus.OK.value(), "Session deleted successfully", null);
        }
        return new ApiResponse(HttpStatus.NOT_FOUND.value(), "Session not found", null);
    }

    @PutMapping("/status/{sessionId}")
    public ApiResponse updateStatus(@PathVariable Long sessionId, @RequestBody StatusDto status) {
        Session session = sessionService.updateStatus(sessionId, status.getStatus());
        return new ApiResponse(HttpStatus.CREATED.value(), "Session updated successfully", session);
    }

    @PutMapping
    public ApiResponse updateSession(@RequestBody SessionDto sessionDto, @RequestParam Long sessionId) {
        SessionDto updatedSession = CustomObjectMapper.convert(sessionService.updateSession(sessionDto, sessionId), SessionDto.class);
        return new ApiResponse(HttpStatus.CREATED.value(), "Session updated successfully", updatedSession);
    }

    @PostMapping("/postMom/{sessionId}")
    public ApiResponse postMOM(@PathVariable Long sessionId, @RequestBody Mommessage message) {
        Session session = sessionService.postMOM(sessionId, message.getMessage());

        return new ApiResponse(HttpStatus.CREATED.value(), "Session updated successfully", session);
    }
}
