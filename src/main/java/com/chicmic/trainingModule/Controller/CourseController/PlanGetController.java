package com.chicmic.trainingModule.Controller.CourseController;

import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponse;
import com.chicmic.trainingModule.Entity.AssignedPlan;
import com.chicmic.trainingModule.Entity.Course;
import com.chicmic.trainingModule.Service.AssignTaskService.AssignTaskService;
import com.chicmic.trainingModule.Service.CourseServices.CourseService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/v1/training/course")
@AllArgsConstructor
public class PlanGetController {
    private final CourseService courseService;
    private final AssignTaskService assignTaskService;
    @GetMapping("/phase/{courseId}")
    public ApiResponse getPhases(@RequestParam(required = true) String courseId,
                                 @RequestParam(required = true) String traineeId,
                                 HttpServletResponse response){
        AssignedPlan assignedPlan = assignTaskService.getAllAssignTasksByTraineeId(traineeId);
        Course course = courseService.getCourseById(courseId);
        if(course == null) {
            return new ApiResponse(HttpStatus.BAD_REQUEST.value(), "Course Does Not Exist", null, response);
        }
        return new ApiResponse(HttpStatus.OK.value(), course.getPhases().size() + " Phases Found", course.getPhases(), response);
    }
    @GetMapping("/task/{courseId}")
    public ApiResponse getTasks(@PathVariable String courseId, HttpServletResponse response){
        Course course = courseService.getCourseById(courseId);
        if(course == null) {
            return new ApiResponse(HttpStatus.BAD_REQUEST.value(), "Course Does Not Exist", null, response);
        }

        return new ApiResponse(HttpStatus.OK.value(), course.getPhases().size() + " Phases Found", course.getPhases(), response);
    }
}
