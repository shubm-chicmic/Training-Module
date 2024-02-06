package com.chicmic.trainingModule.Controller.DropDown;

import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponse;
import com.chicmic.trainingModule.Dto.AssignedPlanFeedbackResponseDto.AssignedPlanResponse;
import com.chicmic.trainingModule.Dto.UserTimeDto.AssignedPlanDto;
import com.chicmic.trainingModule.Entity.AssignedPlan;
import com.chicmic.trainingModule.Service.AssignTaskService.AssignPlanResponseMapper;
import com.chicmic.trainingModule.Service.AssignTaskService.AssignTaskService;
import com.chicmic.trainingModule.TrainingModuleApplication;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/v1/training/dropdown")
@AllArgsConstructor
@PreAuthorize("hasAnyAuthority('TL', 'PA', 'PM','IND', 'TR')")
public class AssignedPlanDropdown {
    private final AssignTaskService assignTaskService;
    private final AssignPlanResponseMapper assignPlanResponseMapper;


    @RequestMapping(value = {"/plan"}, method = RequestMethod.GET)
    public ResponseEntity<ApiResponse> getAll(
            @RequestParam String traineeId,
            HttpServletResponse response,
            Principal principal
    )  {
        AssignedPlan assignedPlan = assignTaskService.getAllAssignTasksByTraineeId(traineeId);
        if(assignedPlan != null) {
            AssignedPlanResponse assignedPlanResponse = assignPlanResponseMapper.mapAssignedPlanForFeedback(assignedPlan);
            return ResponseEntity.ok(new ApiResponse(HttpStatus.OK.value(), "Success", assignedPlanResponse));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse(HttpStatus.BAD_REQUEST.value(), "AssignedPlan not found", null));

    }

    @RequestMapping(value = {"/assignedPlan"}, method = RequestMethod.GET)
    public ResponseEntity<ApiResponse>  getAssignedPlans(
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false) Integer planType,
            @RequestParam(required = false) String milestoneId,
            @RequestParam(required = false) String taskId,
            Principal principal,
            HttpServletResponse response
    )  {

        String traineeId = principal.getName();
//        System.out.println("Trainee id : " + traineeId);
//        System.out.println("trainee name : " + TrainingModuleApplication.idUserMap.get(traineeId).getName());
//        System.out.println("Project : " + projectId);
//        System.out.println("trainee name : " + planType);
//        System.out.println("trainee name : " + milestoneId);
//        System.out.println("trainee name : " + taskId);



        AssignedPlan assignedPlan = assignTaskService.getAllAssignTasksByTraineeId(traineeId);
        if(assignedPlan == null){
            return ResponseEntity.badRequest().body(new ApiResponse(HttpStatus.BAD_REQUEST.value(), "No Plan not found", null));

        }
        AssignedPlanDto assignedPlanDto = null;
        if(traineeId != null && projectId == null &&  milestoneId == null && taskId == null){
            // give all plans
            assignedPlanDto = assignPlanResponseMapper.mapAssignedPlanWithPlanForTimeSheet(assignedPlan);
        }else if (traineeId != null && (projectId != null && planType != null) && milestoneId == null && taskId == null){
            // give all phases of plan
            assignedPlanDto = assignPlanResponseMapper.mapAssignedPlanWithPlanAndPhasesForTimeSheet(assignedPlan, projectId, planType);
        }else if(traineeId != null && (projectId != null && planType != null) && milestoneId != null && taskId == null){
            // give all plan and perticular phase having all tasks
            assignedPlanDto = assignPlanResponseMapper.mapAssignedPlanWithPlanAndPhaseAndMultipleTaskForTimeSheet(assignedPlan, projectId, milestoneId, planType);
        }else if (traineeId != null && (projectId != null && planType != null) && milestoneId != null && taskId != null){
            // give all plan with perticualr phase perticualr task
            System.out.println("Im in//////////////////////////////");
            assignedPlanDto = assignPlanResponseMapper.mapAssignedPlanWithPlanAndPhaseAndTaskForTimeSheet(assignedPlan, projectId, milestoneId, planType, taskId);
        }
//        return new ApiResponse(HttpStatus.OK.value(), "Plan Data Fetched Successfully", assignedPlanDto, response);
        return ResponseEntity.ok(new ApiResponse(HttpStatus.OK.value(), "Plan Data Fetched Successfully", assignedPlanDto));

    }

}
