package com.chicmic.trainingModule.Controller.SessionController;

import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponse;
import com.chicmic.trainingModule.Entity.Session;
import com.chicmic.trainingModule.Service.SessionService.SessionService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/v1/training/session")
@AllArgsConstructor
public class SessionApprove {
    private final SessionService sessionService;

    @PostMapping("/approve/{sessionId}")
    public ApiResponse sessionApprove(@PathVariable String sessionId, Principal principal) {
        Session session = sessionService.getSessionById(sessionId);
        if (session != null) {
            List<String> approver = session.getApprover();
            if (approver.contains(principal.getName())) {

                sessionService.approve(session, principal.getName());
                return new ApiResponse(HttpStatus.OK.value(), "Session approved successfully", null);

            } else {
                return new ApiResponse(HttpStatus.FORBIDDEN.value(), "You are not authorized to approve this session", null);
            }
        } else {
            return new ApiResponse(HttpStatus.NOT_FOUND.value(), "Session not found", null);
        }

    }
}
