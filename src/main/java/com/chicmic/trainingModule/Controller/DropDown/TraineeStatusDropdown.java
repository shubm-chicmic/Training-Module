package com.chicmic.trainingModule.Controller.DropDown;

import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponse;
import com.chicmic.trainingModule.Dto.UserIdAndStatusDto;
import com.chicmic.trainingModule.Service.PlanServices.TraineePlanService_V2;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/v1/training/dropdown")
@AllArgsConstructor
public class TraineeStatusDropdown {
    private final TraineePlanService_V2 traineePlanServiceV2;
    @PatchMapping("/trainee-status")
    public ApiResponse updateTraineeStatus(@RequestBody UserIdAndStatusDto userIdAndStatusDto, Principal principal){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean flag = authentication.getAuthorities().contains("TR");
        if(flag)return new ApiResponse(200,"You Are Not Authorized To Update Status(Role = TR)",null);
        traineePlanServiceV2.updateTraineeStatus(userIdAndStatusDto, principal.getName());
        return new ApiResponse(200,"Trainee status updated successfully!!",null);
    }
}
