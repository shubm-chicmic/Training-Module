package com.chicmic.trainingModule.Controller.UserProgressController;

import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponse;
import com.chicmic.trainingModule.Dto.UserProgressDto;
import com.chicmic.trainingModule.Entity.Constants.EntityType;
import com.chicmic.trainingModule.Entity.Constants.ProgessConstants;
import com.chicmic.trainingModule.Entity.PlanTask;
import com.chicmic.trainingModule.Entity.UserProgress;
import com.chicmic.trainingModule.Service.FeedBackService.FeedbackService_V2;
import com.chicmic.trainingModule.Service.PlanServices.PlanTaskService;
import com.chicmic.trainingModule.Service.UserProgressService.UserProgressService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/training/userProgress")
@AllArgsConstructor
public class UserProgressCRUD {
    private final UserProgressService userProgressService;
    private final PlanTaskService planTaskService;
    private final FeedbackService_V2 feedbackServiceV2;
    @PostMapping
    public ApiResponse createUserProgress(@RequestBody UserProgressDto userProgressDto, Principal principal, HttpServletResponse response) {
        Boolean checked = false;
        if(userProgressDto.getProgressType() == 3 || userProgressDto.getProgressType() == 4){
            PlanTask planTask = planTaskService.getPlanTaskById(userProgressDto.getSubTaskId());
            if(planTask == null){
                return new ApiResponse(HttpStatus.BAD_REQUEST.value(), "Plan task not found", null, response);
            }
            userProgressDto.setSubTaskId(null);
            userProgressDto.setPlanTaskId(planTask.get_id());
            UserProgress userProgress = userProgressService.getUserProgressByTraineeIdPlanIdAndPlanTaskId(
                    userProgressDto.getTraineeId(),
                    userProgressDto.getPlanId(),
                    userProgressDto.getPlanTaskId(),
                    userProgressDto.getProgressType()
            );
            if(userProgress == null) {
                System.out.println("Im in ");
                userProgress = UserProgress.builder()
                        .traineeId(userProgressDto.getTraineeId())
                        .planId(userProgressDto.getPlanId())
                        .planTaskId(userProgressDto.getPlanTaskId())
                        .startDate(LocalDateTime.now())
                        .progressType(userProgressDto.getProgressType())
                        .status(userProgressDto.getStatus())
                        .build();

            }else {
                userProgress.setStatus(userProgressDto.getStatus());
            }
//            if(userProgress.getStatus() == ProgessConstants.Completed){
//
//                List<String> milestonesIds = new ArrayList<>();
//                if(planTask.getMilestones() == null) {
//                    milestonesIds = new ArrayList<>();
//                }else {
//                    if(planTask != null && planTask.getMilestones() != null) {
//                        milestonesIds = planTask.getMilestones().stream()
//                                .map(Object::toString)
//                                .collect(Collectors.toList());
//                    }else {
//                        milestonesIds = new ArrayList<>();
//                    }
//                }
//
//                //delete feedback
//                Boolean isFeedbackExist = feedbackServiceV2.feedbackExistOnParticularPhaseOfTrainee(
//                        userProgress.getTraineeId(),
//                        planTask.getPlan(),
//                        milestonesIds,
//                        String.valueOf(userProgressDto.getProgressType())
//                );
//                if(isFeedbackExist)
//                return new ApiResponse(HttpStatus.BAD_REQUEST.value(), "Feedback already Given", null);
//            }
            System.out.println("viva or ppt feedback is created");
            System.out.println("UserProgress " + userProgress);
            userProgress = userProgressService.createUserProgress(userProgress);
        }else {
            UserProgress userProgress = userProgressService.getUserProgress(userProgressDto);
            if (userProgress == null) {
                userProgress = UserProgress.builder()
                        .traineeId(userProgressDto.getTraineeId())
                        .planId(userProgressDto.getPlanId())
                        .courseId(userProgressDto.getCourseId())
                        .subTaskId(userProgressDto.getSubTaskId())
                        .startDate(LocalDateTime.now())
                        .progressType(userProgressDto.getProgressType())
                        .status(userProgressDto.getStatus())
                        .build();

            } else {
                userProgress.setStatus(userProgressDto.getStatus());
            }
            userProgress = userProgressService.createUserProgress(userProgress);
//            if (userProgressService.getUserProgressByTraineeIdPlanIdAndCourseId(
//                    userProgressDto.getTraineeId(),
//                    userProgressDto.getPlanId(),
//                    userProgressDto.getCourseId(),
//                    EntityType.COURSE
//            ) == null) {
//                UserProgress courseProgress = UserProgress.builder()
//                        .traineeId(userProgressDto.getTraineeId())
//                        .planId(userProgressDto.getPlanId())
//                        .courseId(userProgressDto.getCourseId())
//                        .startDate(LocalDateTime.now())
//                        .progressType(EntityType.COURSE)
//                        .status(ProgessConstants.InProgress)
//                        .build();
//                userProgressService.createUserProgress(courseProgress);
//                courseProgress.set_id(null);
//                courseProgress.setCourseId(null);
//                courseProgress.setProgressType(EntityType.PLAN);
//                userProgressService.createUserProgress(courseProgress);

        }
        checked = userProgressDto.getStatus() == ProgessConstants.Completed;
        return new ApiResponse(HttpStatus.OK.value(), checked ? "Task marked complete successfully" : "Task marked incomplete successfully.", userProgressDto, response);
    }

}
