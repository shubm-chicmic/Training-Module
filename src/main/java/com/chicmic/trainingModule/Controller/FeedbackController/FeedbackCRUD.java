package com.chicmic.trainingModule.Controller.FeedbackController;

import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponse;
import com.chicmic.trainingModule.Dto.CourseResponse.CourseResponse;
import com.chicmic.trainingModule.Dto.FeedbackDto.FeedbackRequestDto;
import com.chicmic.trainingModule.Dto.FeedbackResponse;
import com.chicmic.trainingModule.Entity.AssignedPlan;
import com.chicmic.trainingModule.Entity.Constants.TrainingStatus;
import com.chicmic.trainingModule.Entity.Feedback_V2;
import com.chicmic.trainingModule.ExceptionHandling.ApiException;
import com.chicmic.trainingModule.Service.AssignTaskService.AssignTaskService;
import com.chicmic.trainingModule.Service.FeedBackService.FeedbackService;
import com.chicmic.trainingModule.Util.TrimNullValidator.FeedbackType;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;

import static com.chicmic.trainingModule.Util.FeedbackUtil.checkRole;
@RestController
@RequestMapping("/v1/training/feedback")
@PreAuthorize("hasAnyAuthority('TL', 'PA', 'PM','IND', 'TR')")
public class FeedbackCRUD {
    private FeedbackService feedbackService;
    private AssignTaskService assignTaskService;

    public FeedbackCRUD(FeedbackService feedbackService, AssignTaskService assignTaskService) {
        this.feedbackService = feedbackService;
        this.assignTaskService = assignTaskService;
    }
    @GetMapping
    public ApiResponse getFeedbacks(@RequestParam(value = "index", defaultValue = "0", required = false) Integer pageNumber,
                                    @RequestParam(value = "limit", defaultValue = "10", required = false) Integer pageSize,
                                    @RequestParam(value = "searchString", defaultValue = ".*", required = false) String searchString,
                                    @RequestParam(value = "sortDirection", defaultValue = "2", required = false) Integer sortDirection,
                                    @RequestParam(value = "sortKey", defaultValue = "createdAt", required = false) String sortKey,
                                    @RequestParam(value = "_id",defaultValue = "",required = false) String _id,
                                    @RequestParam(value = "feedbackType",defaultValue = "",required = false) Integer feedbackType,
                                    @RequestParam(value = "traineeId",defaultValue = "",required = false) String traineeId,
                                    Principal principal){
        sortDirection = (sortDirection!=1)?-1:1;
       // pageNumber /= pageSize;
        if (pageNumber < 0 || pageSize < 1)
            throw new ApiException(HttpStatus.NO_CONTENT,"invalid pageNumber or pageSize");

        if(checkRole("TR")){
            if(sortKey.equals("reviewerName")||sortKey.equals("reviewerCode")||sortKey.equals("reviewerTeam"))
                sortKey = String.format("userData.%s",sortKey);
            return feedbackService.findTraineeFeedbacks(pageNumber, pageSize, searchString, sortDirection, sortKey,principal.getName());
        }
        if(feedbackType == null || _id == null || _id.isBlank() || traineeId==null || traineeId.isBlank()) {
            if (sortKey.equals("traineeName") || sortKey.equals("traineeCode") || sortKey.equals("traineeTeam"))
                sortKey = String.format("userData.%s", sortKey);
            return feedbackService.findFeedbacksGivenByUser(pageNumber, pageSize, searchString, sortDirection, sortKey, principal.getName());
        }
        List<CourseResponse> courseResponseV2List;
        courseResponseV2List = feedbackService.findFeedbacksByTaskIdAndTraineeIdAndType(_id,traineeId,feedbackType);
            //feedbackList = feedbackService.findFeedbacksByPptIdAndTraineeId(traineeId,"3");

        return new ApiResponse(200,"Feedback fetched successfully", courseResponseV2List);
    }

    @GetMapping("/user/{userId}/plan/{planId}")
    public ApiResponse findFeedbacksOnUserPlan(@RequestParam(value = "index", defaultValue = "0", required = false) Integer pageNumber,
                                               @RequestParam(value = "limit", defaultValue = "10", required = false) Integer pageSize,
                                                 @RequestParam(value = "searchString", defaultValue = ".*", required = false) String searchString,
                                                @RequestParam(value = "sortDirection", defaultValue = "1", required = false) Integer sortDirection,
                                                @RequestParam(value = "sortKey", defaultValue = "createdAt", required = false) String sortKey,
                                               @PathVariable(value = "planId")String planId,
                                               @PathVariable String userId){
        //pageNumber /= pageSize;
        sortDirection = (sortDirection!=1)?-1:1;
        if (pageNumber < 0 || pageSize < 1)
            throw new ApiException(HttpStatus.NO_CONTENT,"invalid pageNumber or pageSize");
        if(sortKey.equals("reviewerName"))
                sortKey = String.format("userData.%s",sortKey);
        return feedbackService.findFeedbacksOnUserPlan(userId,planId,pageNumber,pageSize,searchString,sortDirection,sortKey);
    }
    @GetMapping("/user/{traineeId}/task/{taskId}")
    public ApiResponse getFeedbackByCourse(@PathVariable String traineeId, @PathVariable String taskId,@RequestParam String planId,@RequestParam Integer feedbackType) {
        List<CourseResponse> courseResponseList = feedbackService.findFeedbacksByTaskIdAndTraineeId(taskId,planId,traineeId,feedbackType);
        return new ApiResponse(200,"Feedback fetched successfully for trainee",courseResponseList);
    }

    @GetMapping("/user/{userId}")
    public ApiResponse findCourseAndTestFeedbacksForTrainee(@RequestParam(value = "index", defaultValue = "0", required = false) Integer pageNumber,
                                                            @RequestParam(value = "limit", defaultValue = "10", required = false) Integer pageSize,
                                                            @RequestParam(value = "searchString", defaultValue = "", required = false) String searchString,
                                                            @RequestParam(value = "sortDirection", defaultValue = "2", required = false) Integer sortDirection,
                                                            @RequestParam(value = "sortKey", defaultValue = "createdAt", required = false) String sortKey,
                                                            @PathVariable String userId,@RequestParam(required = false) String _id,
                                                            @RequestParam(required = false) Integer type,
                                                            Principal principal){

//        pageNumber /= pageSize;
        if (pageNumber < 0 || pageSize < 1)
            throw new ApiException(HttpStatus.NO_CONTENT,"invalid pageNumber or pageSize");
        if(checkRole("TR") && !principal.getName().equals(userId))
            throw new ApiException(HttpStatus.BAD_REQUEST,"You can't access this Api!");

        sortDirection = (sortDirection!=1)?-1:1;
        if(_id == null &&  type == null){
            if(sortKey.equals("reviewerName")||sortKey.equals("reviewerCode")||sortKey.equals("reviewerTeam"))
                sortKey = String.format("userData.%s",sortKey);
            return feedbackService.findTraineeFeedbacks(pageNumber, pageSize, searchString, sortDirection, sortKey,userId);
        }

        if(type == 3)
            throw new ApiException(HttpStatus.BAD_REQUEST,"Please enter valid feedbackType.");

        List<CourseResponse>
            feedbackList = feedbackService.findFeedbacksByTaskIdAndTraineeIdAndType(_id,userId, type);
        //List<CourseResponse> responseList = null;//feedbackService.buildFeedbackResponseForCourseAndTest(feedbackList,_id,type);

        return new ApiResponse(200,"List of All feedbacks",feedbackList);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse giveFeedback(@Valid @RequestBody FeedbackRequestDto feedbackRequestDto, Principal principal){
        if (checkRole("TR"))
            throw new ApiException(HttpStatus.BAD_REQUEST,"You are not authorized to update feedback.");

        boolean flag = checkRole("TL")||checkRole("PM")||checkRole("PA");
        System.out.println(flag + "}}}}}}}}}}}}}}}}}}}}}}}}}}}}");
//        FeedbackResponse feedbackResponse = feedbackService.saveFeedbackInDb(feedbackRequestDto, principal.getName());
        com.chicmic.trainingModule.Dto.FeedbackResponseDto.FeedbackResponse feedbackResponse = feedbackService.saveFeedbackInDb(feedbackRequestDto, principal.getName(),flag);
        feedbackResponse.setOverallRating(feedbackService.computeOverallRatingOfTrainee(feedbackRequestDto.getTrainee()));
        ApiResponse apiResponse =  new ApiResponse(201,"Feedback saved successfully",feedbackResponse);
       // apiResponse.setOverallRating(feedbackService.computeOverallRatingOfTrainee(feedbackRequestDto.getTrainee()));
        return apiResponse;
    }

    @PostMapping("/user")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse giveFeedbackToUser(@Valid @RequestBody FeedbackRequestDto feedbackRequestDto, Principal principal,@RequestParam(defaultValue = "0",required = false)Integer q){
        String traineeId = feedbackRequestDto.getTrainee();
        AssignedPlan assignedPlan = assignTaskService.getAllAssignTasksByTraineeId(traineeId);
        if(assignedPlan == null){
            throw new ApiException(HttpStatus.BAD_REQUEST,"Plan Not Found!");
        }
        if(assignedPlan.getTrainingStatus() == TrainingStatus.COMPLETED)
            throw new ApiException(HttpStatus.BAD_REQUEST,"Training Completed Cannot Give the Feedback!");
        if(assignedPlan.getTrainingStatus() == TrainingStatus.CANCELLED)
            throw new ApiException(HttpStatus.BAD_REQUEST,"Training Cancelled Cannot Give the Feedback!");
        if (checkRole("TR"))
            throw new ApiException(HttpStatus.BAD_REQUEST,"You are not authorized to update feedback.");
//        FeedbackResponse feedbackResponse = feedbackService.saveFeedbackInDb(feedbackRequestDto, principal.getName());
        boolean flag = checkRole("TL")||checkRole("PM")||checkRole("PA");
        Feedback_V2 feedback = feedbackService.saveTraineeFeedback(feedbackRequestDto, principal.getName(),flag);

        int type = feedbackRequestDto.getFeedbackType().charAt(0) - '0';
        String taskId = null;
        if(type == FeedbackType.TEST)
            taskId = feedbackRequestDto.getTest();
        else if(type == FeedbackType.VIVA || type == FeedbackType.PPT)
            taskId = feedbackRequestDto.getCourse();

        System.out.println(taskId + "//////");
        var response = feedbackService.computeOverallRatingOfEmployee(feedbackRequestDto.getTrainee(),feedbackRequestDto.getPlanId(),taskId,feedbackRequestDto.getTaskId(),Integer.toString(type));
        //var response = feedbackService.computeOverallRatingOfEmployee(feedbackRequestDto.getTrainee(),feedbackRequestDto.getPlanId(),taskId,feedbackRequestDto.getTaskId(),Integer.toString(type));
        response.put("_id", feedback.get_id());
        double overallPlanRating = feedbackService.computeOverallPlanRatingOfTrainee(feedbackRequestDto.getTrainee());
        response.put("overallPlanRating", overallPlanRating);
        return new ApiResponse(201,"Feedback saved successfully",response);
    }

    @PutMapping
    public ApiResponse updateFeedback(@Valid @RequestBody FeedbackRequestDto feedbackRequestDto,Principal principal){
        if (checkRole("TR"))
            throw new ApiException(HttpStatus.BAD_REQUEST,"You are not authorized to update feedback.");

        com.chicmic.trainingModule.Dto.FeedbackResponseDto.FeedbackResponse feedbackResponse = feedbackService.updateFeedback(feedbackRequestDto,principal.getName());
        double overallRating = feedbackService.computeOverallRatingOfTrainee(feedbackRequestDto.getTrainee());
        feedbackResponse.setOverallRating(overallRating);
        //        return new ApiResponse(200,"Feedback updated successfully",buildFeedbackResponse(feedbackV2));
        ApiResponse apiResponse = new ApiResponse(200,"Feedback updated successfully",feedbackResponse);
//        apiResponse.setOverallRating(overallRating);
        return apiResponse;
    }
    @PutMapping("/user")
    public ApiResponse updateTraineeFeedback(@Valid @RequestBody FeedbackRequestDto feedbackRequestDto,Principal principal,@RequestParam(defaultValue = "0",required = false)Integer q){
        if (checkRole("TR"))
            throw new ApiException(HttpStatus.BAD_REQUEST,"You are not authorized to update feedback.");

        com.chicmic.trainingModule.Dto.FeedbackResponseDto.FeedbackResponse feedbackResponse = feedbackService.updateFeedback(feedbackRequestDto,principal.getName());
//        return new ApiResponse(200,"Feedback updated successfully",buildFeedbackResponse(feedbackV2));

        int type = feedbackRequestDto.getFeedbackType().charAt(0) - '0';
//        var response = feedbackService.computeOverallRating(feedbackRequestDto.getTrainee(),feedbackResponse.getTask().get_id(),type);
        var response = feedbackService.computeOverallRatingOfEmployee(feedbackRequestDto.getTrainee(),feedbackRequestDto.getPlanId(),feedbackResponse.getTask().get_id(),feedbackRequestDto.getTaskId(),Integer.toString(type));
//        var response = feedbackService.computeOverallRating(feedbackRequestDto.getTrainee(),feedbackResponse.getTask().get_id(),feedbackRequestDto.getPlanId(),type);
        response.put("_id", feedbackResponse.get_id());
        double overallPlanRating = feedbackService.computeOverallPlanRatingOfTrainee(feedbackRequestDto.getTrainee());
        response.put("overallPlanRating", overallPlanRating);
//        return new ApiResponse(200,"Feedback updated successfully",response);
        ApiResponse apiResponse = new ApiResponse(200,"Feedback updated successfully",response);
        return apiResponse;
    }
    @DeleteMapping("/{id}")
    public ApiResponse deleteFeedback(@PathVariable String id,Principal principal){
        if (checkRole("TR"))
            throw new ApiException(HttpStatus.BAD_REQUEST,"You are not authorized to update feedback.");

        Feedback_V2 feedbackV2 = feedbackService.deleteFeedbackById(id, principal.getName());
        int type = feedbackV2.getType().charAt(0) - '0';//FeedbackUtil.FEEDBACKS_V2.get(feedbackV2.getType());
        String taskId = (feedbackV2.getDetails().getCourseId()==null)?feedbackV2.getDetails().getTestId():
                feedbackV2.getDetails().getCourseId();

        //var response = feedbackService.computeOverallRating(feedbackV2.getTraineeId(),taskId,type);
        var response = feedbackService.computeOverallRating(feedbackV2.getTraineeId(),taskId,feedbackV2.getPlanId(),type);
        return new ApiResponse(200,"Feedback deleted successfully",response);
    }

    @GetMapping("/{id}")
    public  ApiResponse getFeedbackById(@PathVariable String id){
        FeedbackResponse feedback = feedbackService.getFeedbackById(id);
        return new ApiResponse(200,"Feedback fetched successfully",feedback);
    }

//    @GetMapping("/user/{traineeId}/course/{courseId}")
//    public ApiResponse getFeedbackByCourse(@PathVariable String traineeId, @PathVariable String courseId,@RequestParam String planId) {
//        List<CourseResponse_V2> courseResponseList = feedbackService.findFeedbacksByCourseIdAndPhaseIdAndTraineeId(courseId,planId,traineeId);
//        return new ApiResponse(200,"Feedback fetched successfully for trainee",courseResponseList);
//    }
//
//    @GetMapping("/user/{traineeId}/test/{testId}")
//    public ApiResponse getFeedbackByTest(@PathVariable String traineeId, @PathVariable String testId,@RequestParam String planId) {
//        List<CourseResponse_V2> courseResponseList = feedbackService.findFeedbacksByTestIdAndPMilestoneIdAndTraineeId(testId,planId,traineeId);
//        return new ApiResponse(200,"Feedback fetched successfully for trainee",courseResponseList);
//    }
}
