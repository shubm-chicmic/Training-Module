package com.chicmic.trainingModule.Controller.SessionController;

import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponse;
import com.chicmic.trainingModule.Dto.SessionDto.SessionAttendDto;
import com.chicmic.trainingModule.Entity.Constants.StatusConstants;
import com.chicmic.trainingModule.Entity.Session;
import com.chicmic.trainingModule.Service.SessionService.SessionResponseMapper;
import com.chicmic.trainingModule.Service.SessionService.SessionService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/v1/training/session")
@AllArgsConstructor
@PreAuthorize("hasAnyAuthority('IND', 'TR')")
public class SessionComplete {
    private final SessionService sessionService;
    @PostMapping("/attendSession")
    public ApiResponse attendSession(@RequestParam String sessionId, @RequestBody SessionAttendDto sessionAttendDto, Principal principal) {
        Session session = sessionService.getSessionById(sessionId);
        if(session != null) {
            if(session.getStatus() != StatusConstants.COMPLETED){
                return new ApiResponse(HttpStatus.BAD_REQUEST.value(), "Session Not Completed Yet!", null);
            }
            List<String> trainees = session.getTrainees();
            if(trainees.contains(principal.getName())) {
                session = sessionService.attendedSession(session, principal.getName(), sessionAttendDto);
                return new ApiResponse(HttpStatus.OK.value(), "You Successfully Attended The Session!", null);
            }else {
                return new ApiResponse(HttpStatus.BAD_REQUEST.value(), "You Are Not Added In The Session!", null);
            }
        }else {
            return new ApiResponse(HttpStatus.BAD_REQUEST.value(), "Session Not Found!", null);
        }
    }
}
