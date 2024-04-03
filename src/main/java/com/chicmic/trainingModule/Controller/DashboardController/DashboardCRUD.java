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
import java.util.ArrayList;
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

        List<String> notesForRatingCalculation = new ArrayList<>();
        String point1ForNotes = "The overall rating is determined as the average of all individual ratings.";
        String point2ForNotes = "Course rating is computed as the average of ratings for all courses across all plans.";
        String point3ForNotes = "If the consumed time is less than the estimated time in a course, a 5-star rating is awarded.";
        String point4ForNotes = "A 4-star rating is awarded if the actual time matches the estimated time.";
        String point5ForNotes = "If the consumed time exceeds the estimated time in a course, the percentage increase in consumed time from the estimated time is subtracted from the 4-star rating.";
        notesForRatingCalculation.add(point1ForNotes);
        notesForRatingCalculation.add(point2ForNotes);
        notesForRatingCalculation.add(point3ForNotes);
        notesForRatingCalculation.add(point4ForNotes);
        notesForRatingCalculation.add(point5ForNotes);

        dashboardResponse.setNotes(notesForRatingCalculation);
        return new ApiResponse(200,"Trainee Rating summary",dashboardResponse);
    }
}
