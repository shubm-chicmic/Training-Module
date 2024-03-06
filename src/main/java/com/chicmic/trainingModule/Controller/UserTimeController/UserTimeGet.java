package com.chicmic.trainingModule.Controller.UserTimeController;

import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponse;
import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponseWithCount;
import com.chicmic.trainingModule.Dto.UserDto;
import com.chicmic.trainingModule.Dto.UserTimeDto.UserTimeResponseDto;
import com.chicmic.trainingModule.Entity.UserTime;
import com.chicmic.trainingModule.Service.UserTimeService.UserTimeResponseService;
import com.chicmic.trainingModule.Service.UserTimeService.UserTimeService;
import com.chicmic.trainingModule.TrainingModuleApplication;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.apache.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/v1/training/userTime")
@AllArgsConstructor
@PreAuthorize("hasAnyAuthority('TL', 'PA', 'PM')")
public class UserTimeGet {
    private final UserTimeResponseService userTimeResponseService;
    private final UserTimeService userTimeService;

    @GetMapping
    public ApiResponseWithCount getUserTime(HttpServletResponse response) {
        List<String> traineeId = userTimeService.getUniqueTraineeIds();
        if (traineeId == null) {
            traineeId = new ArrayList<>();
        }
        System.out.println("TraineeId .size " + traineeId.size());
        List<UserTimeResponseDto> userTimeResponseDto = userTimeResponseService.mapUserTimeToResponseDto(traineeId);
        return new ApiResponseWithCount(traineeId == null ? 0 : traineeId.size(), HttpStatus.SC_OK, "User Time Data Fetched Successfully", userTimeResponseDto, response);
    }
}
