package com.chicmic.trainingModule.Controller.FeedbackController;

import com.chicmic.trainingModule.Dto.*;
import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponse;
import com.chicmic.trainingModule.Dto.CourseResponse.CourseResponse;
import com.chicmic.trainingModule.Dto.ratings.Rating;
import com.chicmic.trainingModule.Entity.Feedback;
import com.chicmic.trainingModule.ExceptionHandling.ApiException;
import com.chicmic.trainingModule.Service.FeedBackService.FeedbackService;
import com.chicmic.trainingModule.Util.FeedbackUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Field;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackCRUD {
    private final FeedbackService feedbackService;

    public FeedbackCRUD(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @GetMapping
    public ApiResponse getFeedbacks(@RequestParam(value = "index", defaultValue = "0", required = false) Integer pageNumber,
                                    @RequestParam(value = "limit", defaultValue = "10", required = false) Integer pageSize,
                                    @RequestParam(value = "searchString", defaultValue = "", required = false) String searchString,
                                    @RequestParam(value = "sortDirection", defaultValue = "2", required = false) Integer sortDirection,
                                    @RequestParam(value = "sortKey", defaultValue = "createdAt", required = false) String sortKey,
                                    @RequestParam(value = "_id",defaultValue = "",required = false) String _id,
                                    @RequestParam(value = "feedbackType",defaultValue = "",required = false) Integer feedbackType,
                                    @RequestParam(value = "traineeId",defaultValue = "",required = false) String traineeId,
                                    Principal principal
                                    ){
        pageNumber /= pageSize;
        if (pageNumber < 0 || pageSize < 1)
            throw new ApiException(HttpStatus.NO_CONTENT,"invalid pageNumber or pageSize");
        if(feedbackType == null || _id == null || _id.isBlank() || traineeId==null || traineeId.isBlank()) {
            List<Feedback> feedbackList = feedbackService.findFeedbacks(pageNumber, pageSize, searchString, sortDirection, sortKey, principal.getName());
            // List<FeedbackResponse> feedbackResponseList = new ArrayList<>();
            List<com.chicmic.trainingModule.Dto.FeedbackResponseDto.FeedbackResponse> feedbackResponses = new ArrayList<>();
            for (Feedback feedback : feedbackList) {
                feedbackResponses.add(com.chicmic.trainingModule.Dto.FeedbackResponseDto.FeedbackResponse.buildFeedbackResponse(feedback));
            }
            return new ApiResponse(200, "List of All feedbacks", feedbackResponses);
        }
        if(feedbackType < 1 || feedbackType > 2)
            throw new ApiException(HttpStatus.BAD_REQUEST,"Please enter valid feedbackType.");
        List<Feedback> feedbackList;
        if(feedbackType == 1)
            feedbackList = feedbackService.findFeedbacksByCourseIdAndTraineeId(_id,traineeId,"1");
        else
            feedbackList = feedbackService.findFeedbacksByTestIdAndTraineeId(_id,traineeId,"2");
        List<Reviewer> responseList = feedbackService.buildFeedbackResponseForCourseAndTest(feedbackList);

        return new ApiResponse(200,"List of All feedbacks",responseList);
    }

    @GetMapping("/{id}")
    public  ApiResponse getFeedbackById(@PathVariable String id){
        Optional<Feedback> feedbackOptional = feedbackService.getFeedbackById(id);
        if(feedbackOptional.isEmpty()){
            throw new ApiException(HttpStatus.NOT_FOUND,"No Feedback exist with this Id.");
        }
        Feedback feedback = feedbackOptional.get();
//        FeedbackResponse feedbackResponse = feedbackService.buildFeedbackResponse(feedback);
        FeedbackResponse1 feedbackResponse = feedbackService.buildFeedbackResponseForSpecificFeedback(feedback);
        //com.chicmic.trainingModule.Dto.FeedbackResponseDto.FeedbackResponse feedbackResponse =
         //       com.chicmic.trainingModule.Dto.FeedbackResponseDto.FeedbackResponse.buildFeedbackResponse(feedback);
        //FeedbackResponse1 feedbackResponse = feedbackService.buildFeedbackResponseForSpecificFeedback(feedback);
        //int pos = (feedback.getFeedbackType().charAt(0) - '1');
        //feedback.setFeedbackType(FeedbackUtil.FEEDBACK_TYPE_CATEGORY[pos]);
        return new ApiResponse(200,"Feedback fetched successfully",feedbackResponse);
    }

    @GetMapping("/emp/{id}")
    public ApiResponse getAllFeedbacksOfEmployeeById(@PathVariable String id){
        List<Feedback> feedbackList = feedbackService.getAllFeedbacksOfEmployeeById(id);

        List<FeedbackResponse> feedbackResponseList = new ArrayList<>();
        for (Feedback feedback : feedbackList)
            feedbackResponseList.add(feedbackService.buildFeedbackResponse(feedback));
        return new ApiResponse(200,"Feedback fetched successfully for trainee",feedbackResponseList);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse feedback(@Valid  @RequestBody FeedBackDto feedBackDto,Principal principal){
        //System.out.println(principal.getName() + "///");
        Feedback feedback = feedbackService.saveFeedbackInDB(feedBackDto,principal.getName());
        com.chicmic.trainingModule.Dto.FeedbackResponseDto.FeedbackResponse feedbackResponse =
        com.chicmic.trainingModule.Dto.FeedbackResponseDto.FeedbackResponse.buildFeedbackResponse(feedback);
        return new ApiResponse(201,"Feedback successfully given to a user",feedbackResponse);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse updateFeedback(@Valid @RequestBody FeedBackDto feedBackDto,Principal principal){
//        System.out.println(principal.getName() + "-----------------");
        Feedback feedback = feedbackService.updateFeedback(feedBackDto,principal.getName());
        if(feedback == null)
            throw new ApiException(HttpStatus.UNAUTHORIZED,"Something Went Wrong");
        FeedbackResponse feedbackResponse = feedbackService.buildFeedbackResponse(feedback);

        return new ApiResponse(200,"FeedBack updated successfully",feedbackResponse);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse deleteFeedbackById(@PathVariable String id,Principal principal){
      //  System.out.println(principal.getName() + "???????????????????");
        feedbackService.deleteFeedbackById(id, principal.getName());
        return new ApiResponse(200,"Feedback Deleted Successfully!!",null);
    }

    @GetMapping("/course")
    public ApiResponse getAllFeedbacksOfTraineeOnCourseWithId(@RequestParam String courseId, @RequestParam String traineeId,Principal principal) throws IllegalAccessException {
         List<Feedback> feedbackList = feedbackService.getAllFeedbacksOfTraineeOnCourseWithId(traineeId,courseId);
         List<FeedbackResponseForCourse> feedbackResponseForCourses = new ArrayList<>();
         for (Feedback feedback : feedbackList){
             FeedbackResponseForCourse feedbackResponseForCourse = feedbackService.buildFeedbackResponseForCourse(feedback);
             feedbackResponseForCourses.add(feedbackResponseForCourse);
         }

         /*
         List<HashMap<String,Object>> res = new ArrayList<>();
         for(Feedback feedback : feedbackList){
             Rating rating = feedback.getRating();
             HashMap<String,Object> feedbackRes = new HashMap<>();
             Class<?> clazz = rating.getClass();
             Field[] fields = clazz.getDeclaredFields();
             for(Field field : fields){
                 field.setAccessible(true);
                 //System.out.println(field.getName() + ":" + field.get(feedback));
                 feedbackRes.put(field.getName(),field.get(rating));
             }
             res.add(feedbackRes);
         }
          */

         return new ApiResponse(200,"fnsa",feedbackResponseForCourses);
    }
}
