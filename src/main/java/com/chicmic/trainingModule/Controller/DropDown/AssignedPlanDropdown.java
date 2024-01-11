package com.chicmic.trainingModule.Controller.DropDown;

import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponse;
import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponseWithCount;
import com.chicmic.trainingModule.Entity.AssignedPlan;
import com.chicmic.trainingModule.Entity.Plan;
import com.chicmic.trainingModule.Service.AssignTaskService.AssignTaskService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/v1/training/dropdown")
@AllArgsConstructor
public class AssignedPlanDropdown {
    private final AssignTaskService assignTaskService;
    @RequestMapping(value = {"/assignedPlan"}, method = RequestMethod.GET)
    public ApiResponse getAll(
            @RequestParam String traineeId,
            HttpServletResponse response,
            Principal principal
    )  {
        AssignedPlan assignedPlan = assignTaskService.getAllAssignTasksByTraineeId(traineeId);
        List<Plan> plans = assignedPlan.getPlans();
        if(assignedPlan != null) {
            return new ApiResponse(HttpStatus.OK.value(), "Success", plans, response);
        }
        return new ApiResponse(HttpStatus.BAD_REQUEST.value(), "AssignedPlan not found", response);
    }
}
