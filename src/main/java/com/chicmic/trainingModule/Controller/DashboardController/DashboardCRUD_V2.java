package com.chicmic.trainingModule.Controller.DashboardController;

import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponse;
import com.chicmic.trainingModule.Dto.DashboardDto.DashboardResponse;
import com.chicmic.trainingModule.ExceptionHandling.ApiException;
import com.chicmic.trainingModule.Service.DashboardService.DashboardService_V2;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/v2/training/dashboard")
public class DashboardCRUD_V2 {
    private final DashboardService_V2 dashboardService;

    public DashboardCRUD_V2(DashboardService_V2 dashboardService) {
        this.dashboardService = dashboardService;
    }
    @GetMapping("/{traineeId}")
    public ApiResponse getTraineeRatingSummary(@PathVariable String traineeId, Principal principal){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean flag = authentication.getAuthorities().contains("TR");
        if(flag && !traineeId.equals(principal.getName()))
            throw new ApiException(HttpStatus.BAD_REQUEST,"You are not allowed to view other dashboard!!");

        DashboardResponse dashboardResponse = dashboardService.getTraineeRatingSummary(traineeId);
        return new ApiResponse(200,"Trainee Rating summary",dashboardResponse);
    }
}
