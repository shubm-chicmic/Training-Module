package com.chicmic.trainingModule.Controller.PlanController;

import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponse;
import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponseWithCount;
import com.chicmic.trainingModule.Dto.PlanDto.PlanDto;
import com.chicmic.trainingModule.Dto.PlanDto.PlanResponseDto;
import com.chicmic.trainingModule.Dto.SessionDto.SessionDto;
import com.chicmic.trainingModule.Entity.Plan.Phase;
import com.chicmic.trainingModule.Entity.Plan.Plan;

import com.chicmic.trainingModule.Entity.Session.Session;
import com.chicmic.trainingModule.Service.PlanServices.PlanService;
import com.chicmic.trainingModule.Util.CustomObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/v1/training/plan")
@AllArgsConstructor
public class PlanCRUD {
    private final PlanService planService;
    @RequestMapping(value = {""}, method = RequestMethod.GET)
    public ApiResponseWithCount getAll(
            @RequestParam(value = "index", defaultValue = "0", required = false) Integer pageNumber,
            @RequestParam(value = "limit", defaultValue = "10", required = false) Integer pageSize,
            @RequestParam(value = "searchString", defaultValue = "", required = false) String searchString,
            @RequestParam(value = "sortDirection", defaultValue = "1", required = false) Integer sortDirection,
            @RequestParam(value = "sortKey", defaultValue = "", required = false) String sortKey,
            @RequestParam(required = false) String planId,
            @RequestParam(required = false, defaultValue = "false") Boolean isPhaseRequired,
            @RequestParam(required = false, defaultValue = "false") Boolean isDropdown,
            HttpServletResponse response
    ) {
        System.out.println("dropdown key = " + isDropdown);
        if (isDropdown) {
            List<Plan> planList = planService.getAllPlans(searchString, sortDirection, sortKey);
            Long count = planService.countNonDeletedPlans();
            List<PlanResponseDto> planResponseDtoList = CustomObjectMapper.mapPlanToResponseDto(planList, isPhaseRequired);
            Collections.reverse(planResponseDtoList);
            return new ApiResponseWithCount(count, HttpStatus.OK.value(), planResponseDtoList.size() + " Plans retrieved", planResponseDtoList, response);
        }
        if (planId == null || planId.isEmpty()) {
            pageNumber /= pageSize;
            if (pageNumber < 0 || pageSize < 1)
                return new ApiResponseWithCount(0, HttpStatus.NO_CONTENT.value(), "invalid pageNumber or pageSize", null, response);
            List<Plan> planList = planService.getAllPlans(pageNumber, pageSize, searchString, sortDirection, sortKey);
            Long count = planService.countNonDeletedPlans();

            List<PlanResponseDto> planResponseDtoList = CustomObjectMapper.mapPlanToResponseDto(planList, isPhaseRequired);
            Collections.reverse(planResponseDtoList);
            return new ApiResponseWithCount(count, HttpStatus.OK.value(), planResponseDtoList.size() + " Plans retrieved", planResponseDtoList, response);
        } else {
            Plan plan = planService.getPlanById(planId);
            if (plan == null) {
                return new ApiResponseWithCount(0, HttpStatus.NOT_FOUND.value(), "Plan not found", null, response);
            }
            PlanResponseDto planResponseDto = CustomObjectMapper.mapPlanToResponseDto(plan);
            return new ApiResponseWithCount(1, HttpStatus.OK.value(), "Plan retrieved successfully", planResponseDto, response);
        }
    }

    @PostMapping
    public ApiResponse create(@RequestBody PlanDto planDto, Principal principal) {
        System.out.println("\u001B[33m planDto previos = " + planDto);
        Plan plan = planService.createPlan(CustomObjectMapper.convert(planDto, Plan.class), principal);
        return new ApiResponse(HttpStatus.CREATED.value(), "Plan created successfully", plan);
    }

    @DeleteMapping("/{planId}")
    public ApiResponse delete(@PathVariable String planId) {
        System.out.println("planId = " + planId);
        Boolean deleted = planService.deletePlanById(planId);
        if (deleted) {
            return new ApiResponse(HttpStatus.OK.value(), "Plan deleted successfully", null);
        }
        return new ApiResponse(HttpStatus.NOT_FOUND.value(), "Plan not found", null);
    }

    @PutMapping
    public ApiResponse updatePlan(@RequestBody PlanDto planDto, @RequestParam String planId, Principal principal, HttpServletResponse response) {
        Plan plan = planService.getPlanById(planId);
        if (plan != null) {
            if (planDto != null && planDto.getApproved() == true) {
                Set<String> approver = plan.getReviewers();
                if (approver.contains(principal.getName())) {
                    plan = planService.approve(plan, principal.getName());
                } else {
                    return new ApiResponse(HttpStatus.FORBIDDEN.value(), "You are not authorized to approve this plan", null, response);
                }
            }
            planDto.setApproved(plan.getApproved());

            PlanResponseDto planResponseDto = CustomObjectMapper.mapPlanToResponseDto(planService.updatePlan(planDto, planId));
            return new ApiResponse(HttpStatus.CREATED.value(), "Plan updated successfully", planResponseDto, response);
        } else {
            return new ApiResponse(HttpStatus.NOT_FOUND.value(), "Plan not found", null, response);
        }
    }
}
