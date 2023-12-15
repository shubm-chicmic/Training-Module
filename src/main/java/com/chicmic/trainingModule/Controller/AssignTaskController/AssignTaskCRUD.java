package com.chicmic.trainingModule.Controller.AssignTaskController;

import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponse;
import com.chicmic.trainingModule.Dto.AssignTaskDto;
import com.chicmic.trainingModule.Dto.CourseDto.CourseDto;
import com.chicmic.trainingModule.Entity.AssignTask.AssignTask;
import com.chicmic.trainingModule.Service.AssignTaskService.AssignTaskService;
import com.chicmic.trainingModule.Service.CourseServices.CourseService;
import com.chicmic.trainingModule.Util.CustomObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/v1/training/assign")
@AllArgsConstructor
public class AssignTaskCRUD {
    private final AssignTaskService assignTaskService;
    @PostMapping
    public ApiResponse create(@RequestBody AssignTaskDto assignTaskDto, Principal principal) {
        System.out.println("assignTaskDto = " + assignTaskDto);
        AssignTask assignTask = assignTaskService.createAssignTask(assignTaskDto, principal);
        return new ApiResponse(HttpStatus.CREATED.value(), "AssignTask created successfully", assignTaskDto);
    }

}
