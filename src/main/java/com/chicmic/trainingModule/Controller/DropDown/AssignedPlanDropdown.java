package com.chicmic.trainingModule.Controller.DropDown;

import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponse;
import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponseWithCount;
import com.chicmic.trainingModule.Dto.AssignedPlanFeedbackResponseDto.AssignedPlanResponse;
import com.chicmic.trainingModule.Dto.UserIdAndStatusDto;
import com.chicmic.trainingModule.Entity.AssignedPlan;
import com.chicmic.trainingModule.Entity.Plan;
import com.chicmic.trainingModule.Service.AssignTaskService.AssignPlanResponseMapper;
import com.chicmic.trainingModule.Service.AssignTaskService.AssignTaskService;
import com.chicmic.trainingModule.Service.PlanServices.TraineePlanService_V2;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/v1/training/dropdown")
@AllArgsConstructor
public class AssignedPlanDropdown {
    private final AssignTaskService assignTaskService;
    private final AssignPlanResponseMapper assignPlanResponseMapper;
    private final TraineePlanService_V2 traineePlanServiceV2;

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
    @PatchMapping("/trainee-status")
    public ApiResponse updateTraineeStatus(@RequestBody UserIdAndStatusDto userIdAndStatusDto){
        traineePlanServiceV2.updateTraineeStatus(userIdAndStatusDto);
        return new ApiResponse(200,"Trainee status updated successfully!!",null);
    }
}
