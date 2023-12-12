package com.chicmic.trainingModule.Controller.FeedbackController;

import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponse;
import com.chicmic.trainingModule.Dto.FeedBackDto;
import com.chicmic.trainingModule.Entity.Feedback;
import com.chicmic.trainingModule.ExceptionHandling.ApiException;
import com.chicmic.trainingModule.Service.FeedBackService.FeedbackService;
import com.chicmic.trainingModule.Util.FeedbackUtil;
import jakarta.validation.Valid;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackCRUD {
    private final FeedbackService feedbackService;

    public FeedbackCRUD(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }
    @GetMapping()
    public ApiResponse getFeedbacks(){
        List<Feedback> feedbackList = feedbackService.findFeedbacks();
        return new ApiResponse(200,"List of All feedbacks",feedbackList);
    }
    @GetMapping("/{id}")
    public  ApiResponse getFeedbackById(@PathVariable String id){
        Optional<Feedback> feedbackOptional = feedbackService.getFeedbackById(id);
        if(feedbackOptional.isEmpty()){
            throw new ApiException(HttpStatus.NOT_FOUND,"No Feedback exist with this Id.");
        }
        Feedback feedback = feedbackOptional.get();
        int pos = (feedback.getFeedbackType().charAt(0) - '1');
        feedback.setFeedbackType(FeedbackUtil.FEEDBACK_TYPE_CATEGORY[pos]);
        return new ApiResponse(200,"Feedback fetched successfully",feedback);
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
        System.out.println(principal.getName() + "-----------------");
        Feedback feedback = feedbackService.updateFeedback(feedBackDto,principal.getName());
        if(feedback == null)
            throw new ApiException(HttpStatus.UNAUTHORIZED,"Something Went Wrong");

        return new ApiResponse(200,"FeedBack updated successfully",feedback);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse deleteFeedbackById(@PathVariable String id,Principal principal){
      //  System.out.println(principal.getName() + "???????????????????");
        feedbackService.deleteFeedbackById(id, principal.getName());
        return new ApiResponse(200,"Feedback Deleted Successfully!!",null);
    }
}
