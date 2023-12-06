package com.chicmic.trainingModule.Controller.SessionController;

import com.chicmic.trainingModule.Dto.*;
import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponse;
import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponseWithCount;
import com.chicmic.trainingModule.Entity.Session;
import com.chicmic.trainingModule.Service.SessionService.SessionService;
import com.chicmic.trainingModule.Util.CustomObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/v1/training/session")
@AllArgsConstructor
public class SessionCRUD {
    private final SessionService sessionService;
    private final RestTemplate restTemplate;

    @GetMapping
    public ApiResponseWithCount getAll(
            @RequestParam(value = "index", defaultValue = "0", required = false) Integer pageNumber,
            @RequestParam(value = "limit", defaultValue = "10", required = false) Integer pageSize
    ) throws JsonProcessingException {
        pageNumber /= pageSize;
        Long count = sessionService.getTotalNonDeletedSessions();
        System.out.println("count = " + count);
        if (pageNumber < 0 || pageSize < 1) return new ApiResponseWithCount(count,HttpStatus.NO_CONTENT.value(), "invalid pageNumber or pageSize", null);
        List<Session> sessionList = sessionService.getAllSessions(pageNumber, pageSize);
        List<SessionResponseDto> sessionResponseDtoList = CustomObjectMapper.mapSessionToResponseDto(sessionList);
        Collections.reverse(sessionResponseDtoList);
        return new ApiResponseWithCount(count,HttpStatus.OK.value(), sessionResponseDtoList.size() + " Sessions retrieved", sessionResponseDtoList);

    }
    @GetMapping("/{sessionId}")
    public ApiResponse get(@PathVariable String sessionId) {
        Session session = sessionService.getSessionById(sessionId);
        SessionResponseDto sessionResponseDto = CustomObjectMapper.mapSessionToResponseDto(session);
        return new ApiResponse(HttpStatus.OK.value(), "Session retrieved successfully", sessionResponseDto);
    }


    @PostMapping
    public ApiResponse create(@RequestBody SessionDto sessionDto, Principal principal) {
        System.out.println("sessionDto = " + sessionDto);
        sessionDto.setCreatedBy(principal.getName());
        sessionDto = CustomObjectMapper.convert(sessionService.createSession(CustomObjectMapper.convert(sessionDto, Session.class)), SessionDto.class);
        return new ApiResponse(HttpStatus.CREATED.value(), "Session created successfully", sessionDto);
    }

    @DeleteMapping("/{sessionId}")
    public ApiResponse delete(@PathVariable String sessionId) {
        System.out.println("sessionId = " + sessionId);
        Boolean deleted = sessionService.deleteSessionById(sessionId);
        if (deleted) {
            return new ApiResponse(HttpStatus.OK.value(), "Session deleted successfully", null);
        }
        return new ApiResponse(HttpStatus.NOT_FOUND.value(), "Session not found", null);
    }

    @PutMapping("/status/{sessionId}")
    public ApiResponse updateStatus(@PathVariable String sessionId, @RequestBody StatusDto status) {
        Session session = sessionService.updateStatus(sessionId, status.getStatus());
        return new ApiResponse(HttpStatus.CREATED.value(), "Session updated successfully", session);
    }

    @PutMapping
    public ApiResponse updateSession(@RequestBody SessionDto sessionDto, @RequestParam String _id) {
        SessionDto updatedSession = CustomObjectMapper.convert(sessionService.updateSession(sessionDto, _id), SessionDto.class);
        return new ApiResponse(HttpStatus.CREATED.value(), "Session updated successfully", updatedSession);
    }

    @PostMapping("/postMom/{sessionId}")
    public ApiResponse postMOM(@PathVariable String sessionId, @RequestBody Mommessage message) {
        Session session = sessionService.postMOM(sessionId, message.getMessage());

        return new ApiResponse(HttpStatus.CREATED.value(), "Session updated successfully", session);
    }
}
