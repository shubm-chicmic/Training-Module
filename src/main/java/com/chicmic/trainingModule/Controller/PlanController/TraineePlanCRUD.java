package com.chicmic.trainingModule.Controller.PlanController;

import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponse;
import com.chicmic.trainingModule.Entity.AssignTask.AssignTask;
import com.chicmic.trainingModule.ExceptionHandling.ApiException;
import com.chicmic.trainingModule.Service.PlanServices.TraineePlanService;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/v1/training/traineeList")
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
        System.out.println("request reaches here!!");
        sortDirection = (sortDirection!=1)?-1:1;
        List<Document> documentList = traineePlanService.fetchUserPlans(pageNumber, pageSize, searchString, sortDirection, sortKey);
        long count = 0;
        count  = mongoTemplate.count(new Query(), AssignTask.class);
        return new ApiResponse(200,"Plan fetched successfully to user",documentList,count);
    }
}

