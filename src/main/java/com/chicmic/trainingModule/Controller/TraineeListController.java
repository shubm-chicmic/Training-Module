package com.chicmic.trainingModule.Controller;

import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponse;
import com.chicmic.trainingModule.TrainingModuleApplication;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/training/trainee")
@AllArgsConstructor
public class TraineeListController {
    @GetMapping
    public ApiResponse getTraineeList() {
        return new ApiResponse(HttpStatus.OK.value(), "Trainee List", TrainingModuleApplication.getTraineeList());
    }
}
