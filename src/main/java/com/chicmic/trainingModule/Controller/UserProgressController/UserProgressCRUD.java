package com.chicmic.trainingModule.Controller.UserProgressController;

import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponse;
import com.chicmic.trainingModule.Dto.UserProgressDto;
import com.chicmic.trainingModule.Entity.Constants.EntityType;
import com.chicmic.trainingModule.Entity.Constants.ProgessConstants;
import com.chicmic.trainingModule.Entity.PlanTask;
import com.chicmic.trainingModule.Entity.UserProgress;
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

@RestController
@RequestMapping("/v1/training/userProgress")
@AllArgsConstructor
public class UserProgressCRUD {
    private final UserProgressService userProgressService;
    private final PlanTaskService planTaskService;
    @PostMapping
    public ApiResponse createUserProgress(@RequestBody UserProgressDto userProgressDto, Principal principal, HttpServletResponse response) {
        if(userProgressDto.getProgressType() == 3 || userProgressDto.getProgressType() == 4){
            PlanTask planTask = planTaskService.getPlanTaskById(userProgressDto.getSubTaskId());
            userProgressDto.setSubTaskId(null);
            userProgressDto.setCourseId(planTask.getPlan());
            UserProgress userProgress = userProgressService.getUserProgressByTraineeIdPlanIdAndCourseId(
                    userProgressDto.getTraineeId(),
                    userProgressDto.getPlanId(),
                    userProgressDto.getCourseId(),
                    userProgressDto.getProgressType()
            );
            if(userProgress == null) {
                userProgress = UserProgress.builder()
                        .traineeId(userProgressDto.getTraineeId())
                        .planId(userProgressDto.getPlanId())
                        .courseId(userProgressDto.getCourseId())
                        .startDate(LocalDateTime.now())
                        .progressType(userProgressDto.getProgressType())
                        .status(userProgressDto.getStatus())
                        .build();

            }else {
                userProgress.setStatus(userProgressDto.getStatus());
            }
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
            if (userProgressService.getUserProgressByTraineeIdPlanIdAndCourseId(
                    userProgressDto.getTraineeId(),
                    userProgressDto.getPlanId(),
                    userProgressDto.getCourseId(),
                    EntityType.COURSE
            ) == null) {
                UserProgress courseProgress = UserProgress.builder()
                        .traineeId(userProgressDto.getTraineeId())
                        .planId(userProgressDto.getPlanId())
                        .courseId(userProgressDto.getCourseId())
                        .startDate(LocalDateTime.now())
                        .progressType(EntityType.COURSE)
                        .status(ProgessConstants.InProgress)
                        .build();
                userProgressService.createUserProgress(courseProgress);
                courseProgress.set_id(null);
                courseProgress.setCourseId(null);
                courseProgress.setProgressType(EntityType.PLAN);
                userProgressService.createUserProgress(courseProgress);
            }
        }
        return new ApiResponse(HttpStatus.OK.value(), "UserProgress created successfully", null, response);
    }

}
