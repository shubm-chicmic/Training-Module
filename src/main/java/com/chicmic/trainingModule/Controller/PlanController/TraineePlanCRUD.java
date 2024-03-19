package com.chicmic.trainingModule.Controller.PlanController;

import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponse;
import com.chicmic.trainingModule.Entity.Filters.Filters;
import com.chicmic.trainingModule.ExceptionHandling.ApiException;
import com.chicmic.trainingModule.Service.PlanServices.TraineePlanService_V2;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequestMapping("/v1/training/traineeList")
@RestController
@PreAuthorize("hasAnyAuthority('TL', 'PA', 'PM','IND', 'TR')")
public class TraineePlanCRUD {
    private final TraineePlanService_V2 traineePlanService;
   private final MongoTemplate mongoTemplate;

    public TraineePlanCRUD(TraineePlanService_V2 traineePlanService, MongoTemplate mongoTemplate) {
        this.traineePlanService = traineePlanService;
        this.mongoTemplate = mongoTemplate;
    }
    @GetMapping
    public ApiResponse fetchAllResponse(@RequestParam(value = "index", defaultValue = "0", required = false) Integer pageNumber,
                                        @RequestParam(value = "limit", defaultValue = "10", required = false) Integer pageSize,
                                        @RequestParam(value = "searchString", defaultValue = "", required = false) String searchString,
                                        @RequestParam(value = "sortDirection", defaultValue = "2", required = false) Integer sortDirection,
                                        @RequestParam(value = "sortKey", defaultValue = "employeeCode", required = false) String sortKey,
                                        @RequestParam(value = "teams", required = false) String teamsParam,
                                        Principal principal
                                        ){
        //pageNumber /= pageSize;

        Set<String> teams = null ;
        if(teamsParam != null ){
            teams = new HashSet<>(Arrays.asList(teamsParam.replaceAll("[\\[\\]\"]", "").split(",")));
        }
        if (pageNumber < 0 || pageSize < 1)
            throw new ApiException(HttpStatus.NO_CONTENT,"invalid pageNumber or pageSize");
        System.out.println("request reaches here!!");
        sortDirection = (sortDirection!=1)?-1:1;
        sortKey = (sortKey.equals("startDate"))?"date":sortKey;
        Filters filters = Filters.builder()
                .teamsFilter(teams)
                .build();
        return traineePlanService.fetchUserPlans(pageNumber, pageSize, searchString, sortDirection, sortKey, principal.getName(),filters);
        //long count  = mongoTemplate.count(new Query(),AssignedPlan.class);
        //return new ApiResponse(200,"Plan fetched successfully to user",documentList,count);
    }
}
