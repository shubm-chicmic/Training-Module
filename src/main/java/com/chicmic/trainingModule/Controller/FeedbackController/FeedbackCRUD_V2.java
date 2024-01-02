package com.chicmic.trainingModule.Controller.FeedbackController;

import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponse;
import com.chicmic.trainingModule.Dto.FeedbackDto.FeedbackRequestDto;
import com.chicmic.trainingModule.Dto.FeedbackResponse_V2;
import com.chicmic.trainingModule.Entity.Feedback_V2;
import com.chicmic.trainingModule.ExceptionHandling.ApiException;
import com.chicmic.trainingModule.Service.FeedBackService.FeedbackService_V2;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;

import static com.chicmic.trainingModule.Dto.FeedbackResponseDto_V2.FeedbackResponse.buildFeedbackResponse;
import static com.chicmic.trainingModule.Util.FeedbackUtil.checkRole;

@RestController
@RequestMapping("/v2/training/feedback")
public class FeedbackCRUD_V2 {
    private FeedbackService_V2 feedbackService;

    public FeedbackCRUD_V2(FeedbackService_V2 feedbackService) {
        this.feedbackService = feedbackService;
    }
    @GetMapping
    public ApiResponse getFeedbacks(@RequestParam(value = "index", defaultValue = "0", required = false) Integer pageNumber,
                                    @RequestParam(value = "limit", defaultValue = "10", required = false) Integer pageSize,
                                    @RequestParam(value = "searchString", defaultValue = ".*", required = false) String searchString,
                                    @RequestParam(value = "sortDirection", defaultValue = "1", required = false) Integer sortDirection,
                                    @RequestParam(value = "sortKey", defaultValue = "createdAt", required = false) String sortKey,
                                    @RequestParam(value = "_id",defaultValue = "",required = false) String _id,
                                    @RequestParam(value = "feedbackType",defaultValue = "",required = false) Integer feedbackType,
                                    @RequestParam(value = "traineeId",defaultValue = "",required = false) String traineeId,
                                    Principal principal){
        sortDirection = (sortDirection!=1)?-1:1;
        pageNumber /= pageSize;
        if (pageNumber < 0 || pageSize < 1)
            throw new ApiException(HttpStatus.NO_CONTENT,"invalid pageNumber or pageSize");

        if(checkRole("TR")){
            if(sortKey.equals("reviewerName")||sortKey.equals("reviewerCode")||sortKey.equals("reviewerTeam"))
                sortKey = String.format("userData.%s",sortKey);
            return feedbackService.findTraineeFeedbacks(pageNumber, pageSize, searchString, sortDirection, sortKey,principal.getName());
        }
        if(sortKey.equals("traineeName")||sortKey.equals("traineeCode")||sortKey.equals("traineeTeam"))
                sortKey = String.format("userData.%s",sortKey);

        List<Integer> integers = new ArrayList<>();
        return feedbackService.findFeedbacksGivenByUser(pageNumber, pageSize, searchString, sortDirection, sortKey, principal.getName());
        //return new ApiResponse(200,"Feedback fetched successfully", integers);
    }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse giveFeedback(@Valid @RequestBody FeedbackRequestDto feedbackRequestDto, Principal principal){
        if (checkRole("TR"))
            throw new ApiException(HttpStatus.BAD_REQUEST,"You are not authorized to update feedback.");

        System.out.println("Fasfasfas..................");
        Feedback_V2 feedback = feedbackService.saveFeedbackInDb(feedbackRequestDto,principal.getName());
        return new ApiResponse(201,"Feedback saved successfully",buildFeedbackResponse(feedback));
    }

    @PutMapping
    public ApiResponse updateFeedback(@Valid @RequestBody FeedbackRequestDto feedbackRequestDto,Principal principal){
        if (checkRole("TR"))
            throw new ApiException(HttpStatus.BAD_REQUEST,"You are not authorized to update feedback.");

        Feedback_V2 feedbackV2 = feedbackService.updateFeedback(feedbackRequestDto,principal.getName());
        return new ApiResponse(200,"Feedback updated successfully",buildFeedbackResponse(feedbackV2));
        //feedbackService.updateFeedback(feedbackRequestDto,"sdasdas");
    }

    @DeleteMapping("/{id}")
    public ApiResponse deleteFeedback(@PathVariable String id,Principal principal){
        if (checkRole("TR"))
            throw new ApiException(HttpStatus.BAD_REQUEST,"You are not authorized to update feedback.");

        String traineeId = feedbackService.deleteFeedbackById(id, principal.getName());
        Map<String,Float> mp = new HashMap<>();
        mp.put("overallRating",4.5f);
        return new ApiResponse(200,"Feedback deleted successfully",mp);
    }
    @GetMapping("/{id}")
    public  ApiResponse getFeedbackById(@PathVariable String id){
        Feedback_V2 feedback = feedbackService.getFeedbackById(id);

        FeedbackResponse_V2 feedbackResponseV2 = FeedbackResponse_V2.buildResponse(feedback);
        // FeedbackResponse feedbackResponse = feedbackService.buildFeedbackResponseForSpecificFeedback(feedback);
        //feedbackResponse = feedbackService.addingPhaseAndTestNameInResponse(feedbackResponse);
        return new ApiResponse(200,"Feedback fetched successfully",feedbackResponseV2);
    }
}
