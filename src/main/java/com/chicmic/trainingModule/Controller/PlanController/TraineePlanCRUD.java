package com.chicmic.trainingModule.Controller.PlanController;

import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponse;
import com.chicmic.trainingModule.Dto.PlanDto.PlanRequestDto;
import com.chicmic.trainingModule.Dto.PlanDto.PlanResponseDto;
import com.chicmic.trainingModule.Dto.TraineePlanReponse;
import com.chicmic.trainingModule.Entity.Plan.UserPlan;
import com.chicmic.trainingModule.ExceptionHandling.ApiException;
import com.chicmic.trainingModule.Service.PlanServices.TraineePlanService;
import jakarta.validation.Valid;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

//@RequestMapping("/v1/training/traineeList")
@RequestMapping("/api/feedback/training/traineeList")
@RestController
public class TraineePlanCRUD {
    private final TraineePlanService traineePlanService;
    private final MongoTemplate mongoTemplate;

    public TraineePlanCRUD(TraineePlanService traineePlanService, MongoTemplate mongoTemplate) {
        this.traineePlanService = traineePlanService;
        this.mongoTemplate = mongoTemplate;
    }

    @GetMapping()
    public ApiResponse fetchAllResponse(@RequestParam(value = "index", defaultValue = "0", required = false) Integer pageNumber,
                                        @RequestParam(value = "limit", defaultValue = "10", required = false) Integer pageSize,
                                        @RequestParam(value = "searchString", defaultValue = "", required = false) String searchString,
                                        @RequestParam(value = "sortDirection", defaultValue = "2", required = false) Integer sortDirection,
                                        @RequestParam(value = "sortKey", defaultValue = "createdAt", required = false) String sortKey){
        pageNumber /= pageSize;
        if (pageNumber < 0 || pageSize < 1)
            throw new ApiException(HttpStatus.NO_CONTENT,"invalid pageNumber or pageSize");

        List<TraineePlanReponse> documentList = traineePlanService.fetchUserPlans(pageNumber, pageSize, searchString, sortDirection, sortKey);
        long count = 0;
        count  = mongoTemplate.count(new Query(), UserPlan.class);
        return new ApiResponse(201,"Plan fetched successfully to user",documentList,count);
    }
    @PostMapping
    public ApiResponse assignMultiplePlansToTrainees(@Valid @RequestBody PlanRequestDto planRequestDto,Principal principal){
//        System.out.println("FGafgasa");
        List<TraineePlanReponse> documentList = traineePlanService.assignMultiplePlansToTrainees(planRequestDto,principal.getName());

        return new ApiResponse(201,"Plan assigned successfully to user",documentList);
    }
}

