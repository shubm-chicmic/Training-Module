package com.chicmic.trainingModule.Controller.DashboardController;

import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponse;
import com.chicmic.trainingModule.Dto.DashboardDto.DashboardResponse;
import com.chicmic.trainingModule.ExceptionHandling.ApiException;
import com.chicmic.trainingModule.Service.DashboardService.DashboardService_V2;
import com.chicmic.trainingModule.Service.SessionService.SessionService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/v1/training/dashboard")
@PreAuthorize("hasAnyAuthority('TL', 'PA', 'PM','IND','TR')")
public class DashboardCRUD {
    private final DashboardService_V2 dashboardService;
    private final SessionService sessionService;

    public DashboardCRUD(DashboardService_V2 dashboardService, SessionService sessionService) {
        this.dashboardService = dashboardService;
        this.sessionService = sessionService;
    }
    @GetMapping("/{traineeId}")
    public ApiResponse getTraineeRatingSummary(@PathVariable String traineeId, Principal principal){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean flag = authentication.getAuthorities().contains("TR");
        if(flag && !traineeId.equals(principal.getName()))
            throw new ApiException(HttpStatus.BAD_REQUEST,"You are not allowed to view other dashboard!!");

        DashboardResponse dashboardResponse = dashboardService.getTraineeRatingSummary(traineeId);
        Long totalAttendedSession = sessionService.countTotalAttendedSessionsByUser(traineeId);
        System.out.println("totalAttendedSession = " + totalAttendedSession);
        dashboardResponse.setAttendedSessions(totalAttendedSession);
        dashboardResponse.setTotalSessions(sessionService.countTotalSessionsForUser(traineeId));
        return new ApiResponse(200,"Trainee Rating summary",dashboardResponse);
    }
}
