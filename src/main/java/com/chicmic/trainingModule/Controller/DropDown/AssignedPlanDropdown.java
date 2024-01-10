package com.chicmic.trainingModule.Controller.DropDown;

import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponse;
import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponseWithCount;
import com.chicmic.trainingModule.Entity.AssignedPlan;
import com.chicmic.trainingModule.Service.AssignTaskService.AssignTaskService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/v1/training/dropdown")
@AllArgsConstructor
public class AssignedPlanDropdown {
    private final AssignTaskService assignTaskService;
//    @RequestMapping(value = {"/assignedPlan"}, method = RequestMethod.GET)
//    public ApiResponseWithCount getAll(
//            @RequestParam String traineeId,
//            @RequestParam String assignedPlanId,
//            HttpServletResponse response,
//            Principal principal
//    )  {
//        AssignedPlan assignedPlan = assignTaskService.getAssignTaskById(assignedPlanId);
//        if(assignedPlan != null) {
//            return new ApiResponse(HttpServletResponse.Ok, "Success", assignTaskService.getAllAssignTasksByTraineeId(traineeId));
//        }
//    }
}
