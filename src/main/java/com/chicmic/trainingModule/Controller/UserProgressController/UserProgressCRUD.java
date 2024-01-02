package com.chicmic.trainingModule.Controller.UserProgressController;

import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponse;
import com.chicmic.trainingModule.Dto.UserProgressDto;
import com.chicmic.trainingModule.Entity.UserProgress;
import com.chicmic.trainingModule.Service.UserProgressService.UserProgressService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/v1/training/userProgress")
@AllArgsConstructor
public class UserProgressCRUD {
    private final UserProgressService userProgressService;
    @PostMapping

    public ApiResponse createUserProgress(@RequestBody UserProgressDto userProgressDto, Principal principal) {
        UserProgress userProgress = UserProgress.builder()
                .progressType(userProgressDto.getProgressType())
                .build();
        userProgress = userProgressService.createUserProgress(userProgress);
        return new ApiResponse(HttpStatus.OK.value(), "UserProgress created successfully", userProgress);
    }

}
