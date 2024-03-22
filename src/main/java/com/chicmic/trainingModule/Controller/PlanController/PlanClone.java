package com.chicmic.trainingModule.Controller.PlanController;

import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponse;
import com.chicmic.trainingModule.Dto.PlanDto.PlanCloneRequestDto;
import com.chicmic.trainingModule.Dto.PlanDto.PlanDto;
import com.chicmic.trainingModule.Entity.Plan;
import com.chicmic.trainingModule.ExceptionHandling.ApiException;
import com.chicmic.trainingModule.Service.PlanServices.PlanResponseMapper;
import com.chicmic.trainingModule.Service.PlanServices.PlanService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/v1/training/plan/clone")
@AllArgsConstructor
public class PlanClone {
    private final PlanService planService;
    private final PlanResponseMapper planResponseMapper;
    @PostMapping
    @PreAuthorize("hasAnyAuthority('TL', 'PA', 'PM') or hasPermission(#planCloneRequestDto, 'canCreatePlan')")
    public ApiResponse planClone(Principal principal, @RequestBody PlanCloneRequestDto planCloneRequestDto) {
        String planId  = planCloneRequestDto.getPlanId();
        if(planId != null) {
            Plan plan = planService.getPlanById(planId);
            if(plan == null) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Plan not found with the given Id");
            }
            Plan clonePlan = planService.clonePlan(plan, principal.getName());
            return new ApiResponse(HttpStatus.OK.value(), "Plan Cloned successfully", clonePlan);
        }else {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Plan Id is Null");
        }
    }
}
