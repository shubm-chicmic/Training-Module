package com.chicmic.trainingModule.Controller.SessionController;

import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponse;
import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponseWithCount;
import com.chicmic.trainingModule.Dto.SessionDto.SessionDto;
import com.chicmic.trainingModule.Dto.SessionDto.SessionResponseDto;
import com.chicmic.trainingModule.Entity.Session;
import com.chicmic.trainingModule.Entity.Constants.StatusConstants;
import com.chicmic.trainingModule.Service.SessionService.SessionResponseMapper;
import com.chicmic.trainingModule.Service.SessionService.SessionService;
import com.chicmic.trainingModule.Util.CustomObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/v1/training/session")
@AllArgsConstructor
public class SessionCRUD {
    private final SessionService sessionService;
    private final SessionResponseMapper sessionResponseMapper;

    @RequestMapping(value = {""}, method = RequestMethod.GET)
    public ApiResponseWithCount getAll(
            @RequestParam(value = "index", defaultValue = "0", required = false) Integer pageNumber,
            @RequestParam(value = "limit", defaultValue = "10", required = false) Integer pageSize,
            @RequestParam(value = "searchString", defaultValue = "", required = false) String searchString,
            @RequestParam(value = "sortDirection", defaultValue = "1", required = false) Integer sortDirection,
            @RequestParam(value = "sortKey", defaultValue = "createdAt", required = false) String sortKey,
            @RequestParam(required = false) String sessionId,
            HttpServletResponse response,
            Principal principal
    ) throws JsonProcessingException {
        if(sessionId == null || sessionId.isEmpty()) {
            pageNumber /= pageSize;
            if (pageNumber < 0 || pageSize < 1)
                return new ApiResponseWithCount(0, HttpStatus.BAD_REQUEST.value(), "invalid pageNumber or pageSize", null, response);
            List<Session> sessionList = sessionService.getAllSessions(pageNumber, pageSize, searchString, sortDirection, sortKey, principal.getName());
            Long count = sessionService.countNonDeletedSessions(searchString, principal.getName());

            List<SessionResponseDto> sessionResponseDtoList = sessionResponseMapper.mapSessionToResponseDto(sessionList);
//            Collections.reverse(sessionResponseDtoList);
            return new ApiResponseWithCount(count, HttpStatus.OK.value(), sessionResponseDtoList.size() + " Sessions retrieved", sessionResponseDtoList, response);
        }else {
            Session session = sessionService.getSessionById(sessionId);
            if(session == null){
                return new ApiResponseWithCount(0,HttpStatus.BAD_REQUEST.value(), "Session not found", null, response);
            }
            SessionResponseDto sessionResponseDto = sessionResponseMapper.mapSessionToResponseDto(session);
            return new ApiResponseWithCount(1,HttpStatus.OK.value(), "Session retrieved successfully", sessionResponseDto, response);
        }
    }

    @PostMapping
    public ApiResponse create(@RequestBody SessionDto sessionDto, Principal principal) {
        System.out.println("sessionDto = " + sessionDto);
        sessionDto.setCreatedBy(principal.getName());
        sessionDto.setStatus(StatusConstants.PENDING);
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

    @PutMapping
    public ApiResponse updateSession(@RequestBody SessionDto sessionDto, @RequestParam String sessionId, Principal principal, HttpServletResponse response) {
        Session session = sessionService.getSessionById(sessionId);

        if (sessionDto.getApprover() != null && sessionDto.getApprover().size() == 0) {
            return new ApiResponse(HttpStatus.BAD_REQUEST.value(), "Reviewers cannot be empty", null, response);
        }
        if (session != null) {
            if(session.getStatus() == StatusConstants.COMPLETED){
                return new ApiResponse(HttpStatus.BAD_REQUEST.value(), "Session is Completed, You Can't Update the session !!", null, response);
            }

            if (sessionDto != null && sessionDto.getApproved() != null) {
                Set<String> approver = session.getApprover();
                if (approver.contains(principal.getName())) {
                    session =sessionService.approve(session, principal.getName(), sessionDto.getApproved());
                } else {
                    return new ApiResponse(HttpStatus.BAD_REQUEST.value(), "You are not authorized to approve this session", null, response);

                }
            }
            sessionDto.setApproved(session.isApproved());
            System.out.println("status = " + sessionDto.getStatus());
            if(sessionDto.getStatus() != null){
                if(sessionDto.getStatus() != StatusConstants.PENDING && sessionDto.getStatus() != StatusConstants.UPCOMING && sessionDto.getStatus() != StatusConstants.COMPLETED) {
                    return new ApiResponse(HttpStatus.BAD_REQUEST.value(), "Status can only be 1 , 2 or 3", null, response);
                }
                if(!session.isApproved()) {
                    return new ApiResponse(HttpStatus.BAD_REQUEST.value(), "You Can't update status since Session is not approved", null, response);
                }
                session = sessionService.updateStatus(sessionId, sessionDto.getStatus());
            }
            sessionDto.setStatus(session.getStatus());
            if(sessionDto != null && sessionDto.getMessage() != null && !sessionDto.getMessage().isEmpty()) {
                if(session.getStatus() == StatusConstants.COMPLETED) {
                    System.out.println("sessionDto = " + sessionDto);
                    if (session.getSessionBy().contains(principal.getName())) {
                        session = sessionService.postMOM(sessionId, sessionDto.getMessage(), principal.getName());
                    } else {
                        return new ApiResponse(HttpStatus.BAD_REQUEST.value(), "You Are Not Authorized to Post MOM", null, response);
                    }
                }
                else {
                    return new ApiResponse(HttpStatus.BAD_REQUEST.value(), "Posting Mom is not allowed when session is not completed", null, response);
                }

            }
            SessionResponseDto sessionResponseDto = sessionResponseMapper.mapSessionToResponseDto(sessionService.updateSession(sessionDto, sessionId));
            return new ApiResponse(HttpStatus.CREATED.value(), "Session updated successfully", sessionResponseDto, response);
        }else {
                return new ApiResponse(HttpStatus.BAD_REQUEST.value(), "Session not found", null, response);
        }
    }

//    @PostMapping("/postMom/{sessionId}")
//    public ApiResponse postMOM(@PathVariable String sessionId, @RequestBody Mommessage message, Principal principal) {
//        Session session = sessionService.getSessionById(sessionId);
//        session = sessionService.postMOM(sessionId, message.getMessage(), principal.getName());
//
//        return new ApiResponse(HttpStatus.CREATED.value(), "Session updated successfully", session);
//    }
}
