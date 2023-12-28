package com.chicmic.trainingModule.Controller.FeedbackController;

import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponse;
import com.chicmic.trainingModule.Dto.CourseResponse.CourseResponse;
import com.chicmic.trainingModule.Dto.FeedBackDto;
import com.chicmic.trainingModule.Dto.FeedbackResponse;
import com.chicmic.trainingModule.Dto.FeedbackResponseForCourse;
import com.chicmic.trainingModule.Entity.Feedback;
import com.chicmic.trainingModule.ExceptionHandling.ApiException;
import com.chicmic.trainingModule.Service.FeedBackService.FeedbackService;
import jakarta.validation.Valid;
import org.bson.Document;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/v1/training/feedback")
public class FeedbackCRUD {
    private final FeedbackService feedbackService;

    public FeedbackCRUD(FeedbackService feedbackService) {
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
                                    Principal principal
                                    ){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean flag = authentication.getAuthorities().contains("TRAINEE");
        sortDirection = (sortDirection!=1)?-1:1;
        if(flag){//trainee
            if(sortKey.equals("reviewerName")||sortKey.equals("reviewerCode")||sortKey.equals("reviewerTeam"))
                sortKey = String.format("userData.%s",sortKey);

//            List<Feedback> feedbackList = feedbackService.findTraineeFeedbacks(pageNumber, pageSize, searchString, sortDirection, sortKey,principal.getName());
//            List<com.chicmic.trainingModule.Dto.FeedbackResponseDto.FeedbackResponse> feedbackResponses = new ArrayList<>();
//            for (Feedback feedback : feedbackList) {
//                feedbackResponses.add(com.chicmic.trainingModule.Dto.FeedbackResponseDto.FeedbackResponse.buildFeedbackResponse(feedback));
//            }
//            return new ApiResponse(200, "List of All feedbacks", feedbackResponses);
            System.out.println("TraineeFeedbacks!!!");
            return feedbackService.findTraineeFeedbacks(pageNumber, pageSize, searchString, sortDirection, sortKey,principal.getName());
        }

        pageNumber /= pageSize;
        if (pageNumber < 0 || pageSize < 1)
            throw new ApiException(HttpStatus.NO_CONTENT,"invalid pageNumber or pageSize");
        if(feedbackType!=null && feedbackType == 3) _id = "adas";
        if(feedbackType == null || _id == null || _id.isBlank() || traineeId==null || traineeId.isBlank()) {
            if(sortKey.equals("traineeName")||sortKey.equals("traineeCode")||sortKey.equals("traineeTeam"))
                sortKey = String.format("userData.%s",sortKey);
            return feedbackService.findFeedbacks(pageNumber, pageSize, searchString, sortDirection, sortKey, principal.getName());
        }
        if(feedbackType < 1 || feedbackType > 3)
            throw new ApiException(HttpStatus.BAD_REQUEST,"Please enter valid feedbackType.");
        List<Feedback> feedbackList;
        if(feedbackType == 1)
            feedbackList = feedbackService.findFeedbacksByCourseIdAndTraineeId(_id,traineeId,"1");
        else if (feedbackType == 2)
            feedbackList = feedbackService.findFeedbacksByTestIdAndTraineeId(_id,traineeId,"2");
        else
            feedbackList = feedbackService.findFeedbacksByPptIdAndTraineeId(traineeId,"3");

        List<CourseResponse> responseList = feedbackService.buildFeedbackResponseForCourseAndTest(feedbackList,_id,feedbackType);

        return new ApiResponse(200,"List of All feedbacks",responseList);
    }

    @GetMapping("/user/{userId}")
    public ApiResponse findCourseAndTestFeedbacksForTrainee(@RequestParam(value = "index", defaultValue = "0", required = false) Integer pageNumber,
                                                            @RequestParam(value = "limit", defaultValue = "10", required = false) Integer pageSize,
                                                            @RequestParam(value = "searchString", defaultValue = "", required = false) String searchString,
                                                            @RequestParam(value = "sortDirection", defaultValue = "1", required = false) Integer sortDirection,
                                                            @RequestParam(value = "sortKey", defaultValue = "createdAt", required = false) String sortKey,
                                                            @PathVariable String userId,@RequestParam(required = false) String _id,
                                                            @RequestParam(required = false) Integer type){
        pageNumber /= pageSize;
        if (pageNumber < 0 || pageSize < 1)
            throw new ApiException(HttpStatus.NO_CONTENT,"invalid pageNumber or pageSize");

        sortDirection = (sortDirection!=1)?-1:1;
        if(_id == null &&  type == null){
            if(sortKey.equals("reviewerName")||sortKey.equals("reviewerCode")||sortKey.equals("reviewerTeam"))
                sortKey = String.format("userData.%s",sortKey);
//            List<Feedback> feedbackList = feedbackService.findTraineeFeedbacks(pageNumber, pageSize, searchString, sortDirection, sortKey,userId);
//            List<com.chicmic.trainingModule.Dto.FeedbackResponseDto.FeedbackResponse> feedbackResponses = new ArrayList<>();
//            for (Feedback feedback : feedbackList) {
//                feedbackResponses.add(com.chicmic.trainingModule.Dto.FeedbackResponseDto.FeedbackResponse.buildFeedbackResponse(feedback));
//            }
//            return new ApiResponse(200, "List of All feedbacks", feedbackResponses);
            return feedbackService.findTraineeFeedbacks(pageNumber, pageSize, searchString, sortDirection, sortKey,userId);
        }

        if(type < 1 || type > 3)
            throw new ApiException(HttpStatus.BAD_REQUEST,"Please enter valid feedbackType.");

        List<Feedback> feedbackList;
        if(type == 1)
            feedbackList = feedbackService.findFeedbacksByCourseIdAndTraineeId(_id,userId,"1");
        else if(type == 2)
            feedbackList = feedbackService.findFeedbacksByTestIdAndTraineeId(_id,userId,"2");
        else
            feedbackList = feedbackService.findFeedbacksByPptIdAndTraineeId(userId,"3");
        List<CourseResponse> responseList = feedbackService.buildFeedbackResponseForCourseAndTest(feedbackList,_id,type);

        return new ApiResponse(200,"List of All feedbacks",responseList);
    }

    @GetMapping("/{id}")
    public  ApiResponse getFeedbackById(@PathVariable String id){
        Feedback feedback = feedbackService.getFeedbackById(id);
        FeedbackResponse feedbackResponse = feedbackService.buildFeedbackResponseForSpecificFeedback(feedback);
        feedbackResponse = feedbackService.addingPhaseAndTestNameInResponse(feedbackResponse);
        return new ApiResponse(200,"Feedback fetched successfully",feedbackResponse);
    }

    @GetMapping("/emp/{id}")
    public ApiResponse getAllFeedbacksOfEmployeeById(@PathVariable String id){
       // feedbackService.getCourseNameAndPhaseName(Arrays.asList("6579b4500cf9d953fe39e2a4"));
        //System.out.println("fsafsa");
        List<Document> feedbackList = feedbackService.getAllFeedbacksOfEmployeeById(id);

        //List<FeedbackResponse> feedbackResponseList = new ArrayList<>();
        //for (Feedback feedback : feedbackList)
          //  feedbackResponseList.add(feedbackService.buildFeedbackResponse(feedback));
        return new ApiResponse(200,"Feedback fetched successfully for trainee",feedbackList);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse feedback(@Valid  @RequestBody FeedBackDto feedBackDto, Principal principal, @RequestParam(defaultValue = "0",required = false)Integer q){
        //System.out.println(principal.getName() + "///");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean flag = authentication.getAuthorities().contains("TRAINEE");
        if(flag)
            throw new ApiException(HttpStatus.BAD_REQUEST,"You are not authorized to give feedback.");

        Feedback feedback = feedbackService.saveFeedbackInDB(feedBackDto, principal.getName());
        com.chicmic.trainingModule.Dto.FeedbackResponseDto.FeedbackResponse feedbackResponse =
                com.chicmic.trainingModule.Dto.FeedbackResponseDto.FeedbackResponse.buildFeedbackResponse(feedback);
        feedbackResponse = feedbackService.addingPhaseAndTestNameInResponse(Arrays.asList(feedbackResponse)).get(0);
        feedbackResponse.setOverallRating(feedbackService.getOverallRatingOfTrainee(feedBackDto.getTrainee()));
        if(q==0)
            return new ApiResponse(201, "Feedback successfully given to a user", feedbackResponse);

        HashMap<String,Object> response = feedbackService.getOverallRatingOfTrainee(feedBackDto.getTrainee(),feedBackDto.getCourse(), feedBackDto.getPhase());
        response.put("_id",feedback.getId());
        return new ApiResponse(201, "Feedback successfully given to a user", response);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse updateFeedback(@Valid @RequestBody FeedBackDto feedBackDto,Principal principal,@RequestParam(defaultValue = "0",required = false)Integer q){
//        System.out.println(principal.getName() + "-----------------");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean flag = authentication.getAuthorities().contains("TRAINEE");
        if(flag)
            throw new ApiException(HttpStatus.BAD_REQUEST,"You are not authorized to give feedback.");

        Feedback feedback = feedbackService.updateFeedback(feedBackDto,principal.getName());
        if(feedback == null)
            throw new ApiException(HttpStatus.UNAUTHORIZED,"Something Went Wrong");

        com.chicmic.trainingModule.Dto.FeedbackResponseDto.FeedbackResponse feedbackResponse =
                com.chicmic.trainingModule.Dto.FeedbackResponseDto.FeedbackResponse.buildFeedbackResponse(feedback);
        feedbackResponse = feedbackService.addingPhaseAndTestNameInResponse(Arrays.asList(feedbackResponse)).get(0);
        feedbackResponse.setOverallRating(feedbackService.getOverallRatingOfTrainee(feedBackDto.getTrainee()));
        if(q==0)
            return new ApiResponse(200,"FeedBack updated successfully",feedbackResponse);

        HashMap<String,Object> response = feedbackService.getOverallRatingOfTrainee(feedBackDto.getTrainee(),feedBackDto.getCourse(), feedBackDto.getPhase());
        response.put("_id",feedback.getId());
        return new ApiResponse(201, "Feedback successfully given to a user", response);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse deleteFeedbackById(@PathVariable String id,Principal principal){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean flag = authentication.getAuthorities().contains("TRAINEE");
        if(flag)
            throw new ApiException(HttpStatus.BAD_REQUEST,"You can't delete this feedback.");

        //  System.out.println(principal.getName() + "???????????????????");
        String traineeId = feedbackService.deleteFeedbackById(id, principal.getName());
        Float overallRating = feedbackService.getOverallRatingOfTrainee(traineeId);
        HashMap<String,Float> mp = new HashMap<>();
        mp.put("overallRating",overallRating);
        return new ApiResponse(200,"Feedback Deleted Successfully!!",mp);
    }

    @GetMapping("/course")
    public ApiResponse getAllFeedbacksOfTraineeOnCourseWithId(@RequestParam String courseId, @RequestParam String traineeId,Principal principal) throws IllegalAccessException {
         List<Feedback> feedbackList = feedbackService.getAllFeedbacksOfTraineeOnCourseWithId(traineeId,courseId);
         List<FeedbackResponseForCourse> feedbackResponseForCourses = new ArrayList<>();
         for (Feedback feedback : feedbackList){
             FeedbackResponseForCourse feedbackResponseForCourse = feedbackService.buildFeedbackResponseForCourse(feedback);
             feedbackResponseForCourses.add(feedbackResponseForCourse);
         }

         return new ApiResponse(200,"fnsa",feedbackResponseForCourses);
    }

    @GetMapping("/phase/{phaseId}")
    public ApiResponse getFeedbackByPhase(@RequestParam String traineeId, @RequestParam String courseId,@PathVariable String phaseId) {
        List<CourseResponse> courseResponseList = feedbackService.findFeedbacksByCourseIdAndPhaseIdAndTraineeId(courseId,phaseId,traineeId);
        return new ApiResponse(200,"Feedback fetched successfully for trainee",courseResponseList);
    }

    @GetMapping("/milestone/{milestoneId}")
    public ApiResponse getFeedbackByMileStone(@RequestParam String traineeId, @RequestParam String testId,@PathVariable String milestoneId) {
        List<CourseResponse> courseResponseList = feedbackService.findFeedbacksByTestIdAndPMilestoneIdAndTraineeId(testId,milestoneId,traineeId);
        return new ApiResponse(200,"Feedback fetched successfully for trainee",courseResponseList);
    }

    @GetMapping("/ppt/{courseId}")
    public ApiResponse getFeedbackByPptAndCourseId(@RequestParam String traineeId,@PathVariable String courseId) {
        List<CourseResponse> courseResponseList = feedbackService.findFeedbacksForCourseByCourseIdAndTraineeId(courseId,traineeId);
        return new ApiResponse(200,"Feedback fetched successfully for trainee",courseResponseList);
    }

}
