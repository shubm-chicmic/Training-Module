package com.chicmic.trainingModule.Controller.FeedbackController;

import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponse;
import com.chicmic.trainingModule.Dto.FeedBackDto;
import com.chicmic.trainingModule.Entity.Feedback;
import com.chicmic.trainingModule.Service.FeedBackService.FeedbackService;
import jakarta.validation.Valid;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackCRUD {
    private final FeedbackService feedbackService;

    public FeedbackCRUD(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse feedback(@Valid  @RequestBody FeedBackDto feedBackDto,Principal principal){
        //System.out.println(principal.getName() + "///");
        Feedback feedback = feedbackService.saveFeedbackInDB(feedBackDto,principal.getName());
        return new ApiResponse(201,"Feedback successfully given to a user",feedback);
    }
    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse updateFeedback(@Valid @RequestBody FeedBackDto feedBackDto,Principal principal){
        return new ApiResponse(200,"FeedBack updated successfully",null);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse deleteFeedbackById(@PathVariable String id,Principal principal){
      //  System.out.println(principal.getName() + "???????????????????");
        feedbackService.deleteFeedbackById(id, principal.getName());
        return new ApiResponse(200,"Feedback Deleted Successfully!!",null);
    }
}
