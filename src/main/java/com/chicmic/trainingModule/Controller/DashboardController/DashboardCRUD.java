package com.chicmic.trainingModule.Controller.DashboardController;

import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponse;
import com.chicmic.trainingModule.Dto.DashboardDto.DashboardResponse;
import com.chicmic.trainingModule.Service.DashboardService.DashboardService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/v1/training/dashboard")
public class DashboardCRUD {
    private final DashboardService dashboardService;

    public DashboardCRUD(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/{traineeId}")
    public ApiResponse getTraineeRatingSummary(@PathVariable String traineeId, Principal principal){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean flag = authentication.getAuthorities().contains("TRAINEE");
        if(flag)
            traineeId = principal.getName();

        DashboardResponse dashboardResponse = dashboardService.getTraineeRatingSummary(traineeId);
        return new ApiResponse(200,"Trainee Rating summary",dashboardResponse);
    }
}
