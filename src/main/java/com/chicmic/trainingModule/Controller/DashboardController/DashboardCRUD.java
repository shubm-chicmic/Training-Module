package com.chicmic.trainingModule.Controller.DashboardController;

import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponse;
import com.chicmic.trainingModule.Dto.DashboardDto.DashboardResponse;
import com.chicmic.trainingModule.Entity.AssignedPlan;
import com.chicmic.trainingModule.Entity.Phase;
import com.chicmic.trainingModule.Entity.Plan;
import com.chicmic.trainingModule.Entity.PlanTask;
import com.chicmic.trainingModule.ExceptionHandling.ApiException;
import com.chicmic.trainingModule.Service.AssignTaskService.AssignTaskService;
import com.chicmic.trainingModule.Service.DashboardService.DashboardService;
import com.chicmic.trainingModule.Service.SessionService.SessionService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/v1/training/dashboard")
@PreAuthorize("hasAnyAuthority('TL', 'PA', 'PM','IND','TR')")
public class DashboardCRUD {
    private final DashboardService dashboardService;
    private final SessionService sessionService;
    private final AssignTaskService assignTaskService;

    public DashboardCRUD(DashboardService dashboardService, SessionService sessionService, AssignTaskService assignTaskService) {
        this.dashboardService = dashboardService;
        this.sessionService = sessionService;
        this.assignTaskService = assignTaskService;
    }
    @GetMapping("/{traineeId}")
    public ApiResponse getTraineeRatingSummary(@PathVariable String traineeId, Principal principal, @RequestHeader("Authorization") String authorizationToken){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean flag = authentication.getAuthorities().contains("TR");
        if(flag && !traineeId.equals(principal.getName()))
            throw new ApiException(HttpStatus.BAD_REQUEST,"You are not allowed to view  dashboard!!");
        flag  = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("IND"));
        if(flag){
            AssignedPlan assignedPlan = assignTaskService.getAllAssignTasksByTraineeId(traineeId);
            List<Plan> plans = assignedPlan.getPlans();
            boolean isRolePermitted = false;
            for (Plan plan : plans){
                for (Phase<PlanTask> phase : plan.getPhases()){
                    for (PlanTask planTask : phase.getTasks()){
                        if(planTask.getMentorIds().contains(principal.getName())){
                            isRolePermitted = true;
                            break;
                        }
                    }
                }
            }
            if(!isRolePermitted)
                throw new ApiException(HttpStatus.BAD_REQUEST,"You are not allowed to view this dashboard!!");
        }
        DashboardResponse dashboardResponse = dashboardService.getTraineeRatingSummary(traineeId);
        Long totalAttendedSession = sessionService.countTotalAttendedSessionsByUser(traineeId);
        System.out.println("totalAttendedSession = " + totalAttendedSession);
        dashboardResponse.setAttendedSessions(totalAttendedSession);
        dashboardResponse.setTotalSessions(sessionService.countTotalSessionsForUser(traineeId));
        return new ApiResponse(200,"Trainee Rating summary",dashboardResponse);
    }
}
