package com.chicmic.trainingModule.Controller.DashboardController;

import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponse;
import com.chicmic.trainingModule.Dto.DashboardDto.DashboardResponse;
import com.chicmic.trainingModule.Service.DashboardService.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feedback/dashboard")
public class DashboardCRUD {
    private final DashboardService dashboardService;

    public DashboardCRUD(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/{traineeId}")
    public ApiResponse getTraineeRatingSummary(@PathVariable String traineeId){
        DashboardResponse dashboardResponse = dashboardService.getTraineeRatingSummary(traineeId);
        return new ApiResponse(200,"Trainee Rating summary",dashboardResponse);
    }
}
