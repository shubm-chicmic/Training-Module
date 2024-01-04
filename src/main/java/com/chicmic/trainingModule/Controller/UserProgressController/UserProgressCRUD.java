package com.chicmic.trainingModule.Controller.UserProgressController;

import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponse;
import com.chicmic.trainingModule.Dto.UserProgressDto;
import com.chicmic.trainingModule.Entity.Constants.EntityType;
import com.chicmic.trainingModule.Entity.Constants.ProgessConstants;
import com.chicmic.trainingModule.Entity.UserProgress;
import com.chicmic.trainingModule.Service.UserProgressService.UserProgressService;
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
    @PostMapping
    public ApiResponse createUserProgress(@RequestBody UserProgressDto userProgressDto, Principal principal) {
        UserProgress userProgress = userProgressService.getUserProgress(userProgressDto);
        if(userProgress == null) {
            userProgress = UserProgress.builder()
                    .traineeId(userProgressDto.getTraineeId())
                    .planId(userProgressDto.getPlanId())
                    .courseId(userProgressDto.getCourseId())
                    .subTaskId(userProgressDto.getSubTaskId())
                    .startDate(LocalDateTime.now())
                    .progressType(userProgressDto.getProgressType())
                    .status(userProgressDto.getStatus())
                    .build();
//            if(userProgressService.getUserProgressByTraineeIdPlanIdAndCourseId(userProgressDto) == null){
//                userProgress = UserProgress.builder()
//                        .traineeId(userProgressDto.getTraineeId())
//                        .planId(userProgressDto.getPlanId())
//                        .courseId(userProgressDto.getCourseId())
//                        .startDate(LocalDateTime.now())
//                        .progressType(EntityType.COURSE)
//                        .status(ProgessConstants.InProgress)
//                        .build();
//                userProgressService.createUserProgress(userProgress);
//                userProgress.setCourseId(null);
//                userProgressService.createUserProgress(userProgress);
//            }
        }else {
            userProgress.setStatus(userProgressDto.getStatus());
        }
        userProgress = userProgressService.createUserProgress(userProgress);
        return new ApiResponse(HttpStatus.OK.value(), "UserProgress created successfully", userProgress);
    }

}
