package com.chicmic.trainingModule.Controller.UserTimeController;

import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponse;
import com.chicmic.trainingModule.Dto.UserTimeDto.UserTimeDto;
import com.chicmic.trainingModule.Entity.UserTime;
import com.chicmic.trainingModule.Service.UserTimeService.UserTimeService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/v1/training/userTime")
@AllArgsConstructor
public class UserTimeCreate {
    private final UserTimeService userTimeService;
   @PostMapping
    public ApiResponse createUserTime(
            UserTimeDto userTimeDto,
            HttpServletResponse response,
            Principal principal
   ){
       if(principal.getName().equals(userTimeDto))
       if(userTimeDto != null){
           try {
               UserTime userTime = userTimeService.createUserTime(userTimeDto, principal.getName());
               return new ApiResponse(HttpStatus.OK.value(), "Time Saved Successfully For the Task", userTime, response);

           }catch (Exception ex){
               return new ApiResponse(HttpStatus.BAD_REQUEST.value(), "Exception Occur While Saving Time!", null, response);
           }

       }
       return new ApiResponse(HttpStatus.BAD_REQUEST.value(), "Request Body is Empty", null, response);

   }
}
