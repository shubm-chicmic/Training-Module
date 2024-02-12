package com.chicmic.trainingModule.Controller.UserTimeController;

import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponse;
import com.chicmic.trainingModule.Dto.UserTimeDto.UserTimeDto;
import com.chicmic.trainingModule.Entity.Constants.TimeSheetType;
import com.chicmic.trainingModule.Entity.UserTime;
import com.chicmic.trainingModule.Service.UserTimeService.UserTimeService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/v1/training/userTime")
@AllArgsConstructor
public class UserTimeCreate {
    private final UserTimeService userTimeService;
   @PostMapping
    public ApiResponse createUserTime(
            @RequestBody UserTimeDto userTimeDto,
            HttpServletResponse response,
            Principal principal
   ){
       if(userTimeDto != null){
           System.out.println("\u001B[35m Usertime Dto : ");
           System.out.println(userTimeDto + "\u001B[0m");
//           try {
           if(userTimeDto.getType() == TimeSheetType.SESSION){
               UserTime userTime = userTimeService.createSessionTimeForUser(userTimeDto, principal.getName());
               return new ApiResponse(HttpStatus.OK.value(), "Time Saved Successfully For the Session", userTime, response);
           }else {
               UserTime userTime = userTimeService.createUserTime(userTimeDto, principal.getName());
               return new ApiResponse(HttpStatus.OK.value(), "Time Saved Successfully For the Task", userTime, response);
           }
//           }catch (Exception ex){
//               return new ApiResponse(HttpStatus.BAD_REQUEST.value(), "Exception Occur While Saving Time!", ex.getMessage(), response);
//           }

       }
       return new ApiResponse(HttpStatus.BAD_REQUEST.value(), "Request Body is Empty", null, response);

   }
}
