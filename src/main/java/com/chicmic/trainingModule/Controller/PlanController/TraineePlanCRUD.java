package com.chicmic.trainingModule.Controller.PlanController;

import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponse;
import com.chicmic.trainingModule.Dto.PlanDto.PlanRequestDto;
import com.chicmic.trainingModule.Dto.PlanDto.PlanResponseDto;
import com.chicmic.trainingModule.Dto.TraineePlanReponse;
import com.chicmic.trainingModule.Service.PlanServices.TraineePlanService;
import jakarta.validation.Valid;
import org.bson.Document;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@RequestMapping("/v1/training/traineeList")
@RestController
public class TraineePlanCRUD {
    private final TraineePlanService traineePlanService;

    public TraineePlanCRUD(TraineePlanService traineePlanService) {
        this.traineePlanService = traineePlanService;
    }

    @PostMapping
    public ApiResponse assignMultiplePlansToTrainees(@Valid @RequestBody PlanRequestDto planRequestDto,Principal principal){
//        System.out.println("FGafgasa");
        List<TraineePlanReponse> documentList = traineePlanService.assignMultiplePlansToTrainees(planRequestDto,principal.getName());

        return new ApiResponse(201,"Plan assigned successfully to user",documentList);
    }
}

