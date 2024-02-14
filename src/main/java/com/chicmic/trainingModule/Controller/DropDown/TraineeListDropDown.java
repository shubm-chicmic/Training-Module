package com.chicmic.trainingModule.Controller.DropDown;

import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponse;
import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponseWithCount;
import com.chicmic.trainingModule.Dto.UserDto;
import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.chicmic.trainingModule.Service.AssignTaskService.AssignTaskService;
import com.chicmic.trainingModule.Service.TraineeService;
import com.chicmic.trainingModule.TrainingModuleApplication;
import lombok.AllArgsConstructor;
import org.apache.tomcat.util.http.parser.Authorization;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/training/dropdown")
@AllArgsConstructor
@PreAuthorize("hasAnyAuthority('TL', 'PA', 'PM','IND','TR')")
public class TraineeListDropDown {
    private final AssignTaskService assignTaskService;
    @GetMapping("/traineeList")
    public ApiResponseWithCount getTraineeData(Principal principal){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Boolean isIndividualRole = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("IND"));
        HashMap<String, UserDto> traineeIdAndObjectMap = TrainingModuleApplication.findTraineeAndMap();
        UserDto currentUserData = TrainingModuleApplication.idUserMap.get(principal.getName());
        List<UserDto> traineeList = new ArrayList<>();
        for (Map.Entry<String, UserDto> entry : traineeIdAndObjectMap.entrySet()) {
            String traineeId = entry.getKey();
            UserDto traineeDto = entry.getValue();
            String traineeName = traineeDto.getEmployeeFullName();
            if(traineeId.equals(principal.getName())){
                traineeList.add(traineeDto);
            }
            else if(isIndividualRole) {
                if ((assignTaskService.isUserMentorOfTrainee(traineeId, principal.getName()) || TraineeService.isUserInSameTeam(traineeDto, currentUserData)))
                    traineeList.add(traineeDto);
            }
            else{
                traineeList.add(traineeDto);
            }
        }
        return new ApiResponseWithCount(traineeList.size(), HttpStatus.OK.value(), "Trainee data Fetched Successfully",traineeList);
    }
}
