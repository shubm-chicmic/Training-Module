package com.chicmic.trainingModule.Controller.DropDown;

import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponse;
import com.chicmic.trainingModule.Dto.AssignedPlanFeedbackResponseDto.AssignedPlanResponse;
import com.chicmic.trainingModule.Dto.UserTimeDto.AssignedPlanDto;
import com.chicmic.trainingModule.Entity.AssignedPlan;
import com.chicmic.trainingModule.Service.AssignTaskService.AssignPlanResponseMapper;
import com.chicmic.trainingModule.Service.AssignTaskService.AssignTaskService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/v1/training/dropdown")
@AllArgsConstructor
public class AssignedPlanDropdown {
    private final AssignTaskService assignTaskService;
    private final AssignPlanResponseMapper assignPlanResponseMapper;


    @RequestMapping(value = {"/plan"}, method = RequestMethod.GET)
    public ApiResponse getAll(
            @RequestParam String traineeId,
            HttpServletResponse response,
            Principal principal
    )  {
        AssignedPlan assignedPlan = assignTaskService.getAllAssignTasksByTraineeId(traineeId);
        if(assignedPlan != null) {
            AssignedPlanResponse assignedPlanResponse = assignPlanResponseMapper.mapAssignedPlanForFeedback(assignedPlan);
            return new ApiResponse(HttpStatus.OK.value(), "Success", assignedPlanResponse, response);
        }
        return new ApiResponse(HttpStatus.BAD_REQUEST.value(), "AssignedPlan not found", response);
    }

    @RequestMapping(value = {"/assignedPlan"}, method = RequestMethod.GET)
    public ApiResponse getAssignedPlans(
            @RequestParam String traineeId,
            @RequestParam(required = false) String planId,
            @RequestParam(required = false) String phaseId,
            @RequestParam(required = false) String taskId,
            HttpServletResponse response,
            Principal principal
    )  {
        AssignedPlan assignedPlan = assignTaskService.getAllAssignTasksByTraineeId(traineeId);
        if(assignedPlan == null){
            return new ApiResponse(HttpStatus.BAD_REQUEST.value(), "No Plan not found", response);
        }
        AssignedPlanDto assignedPlanDto = null;
        if(traineeId != null && planId == null && phaseId == null && taskId == null){
            // give all plans
            assignedPlanDto = assignPlanResponseMapper.mapAssignedPlanWithPlanForTimeSheet(assignedPlan);
        }else if (traineeId != null && planId != null && phaseId == null && taskId == null){
            // give all phases of plan
            assignedPlanDto = assignPlanResponseMapper.mapAssignedPlanWithPlanAndPhasesForTimeSheet(assignedPlan, planId);
        }else if(traineeId != null && planId != null && phaseId != null && taskId == null){
            // give all plan and perticular phase having all tasks
            assignedPlanDto = assignPlanResponseMapper.mapAssignedPlanWithPlanAndPhaseAndMultipleTaskForTimeSheet(assignedPlan, planId, phaseId);
        }else if (traineeId != null && planId != null && phaseId != null && taskId != null){
            // give all plan with perticualr phase perticualr task
            assignedPlanDto = assignPlanResponseMapper.mapAssignedPlanWithPlanAndPhaseAndTaskForTimeSheet(assignedPlan, planId, phaseId, taskId);
        }
        return new ApiResponse(HttpStatus.OK.value(), "Plan Data Fetched Successfully", assignedPlanDto, response);
    }

}
