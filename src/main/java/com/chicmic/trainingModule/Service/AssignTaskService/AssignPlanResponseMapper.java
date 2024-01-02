package com.chicmic.trainingModule.Service.AssignTaskService;


import com.chicmic.trainingModule.Dto.AssignTaskDto.PlanTaskResponseDto;
import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.chicmic.trainingModule.Entity.Course;
import com.chicmic.trainingModule.Entity.PlanTask;
import com.chicmic.trainingModule.Entity.Test;
import com.chicmic.trainingModule.Service.CourseServices.CourseService;
import com.chicmic.trainingModule.Service.TestServices.TestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AssignPlanResponseMapper {
    private final TestService testService;
    private final CourseService courseService;
    public List<PlanTaskResponseDto> mapAssignPlanToResponseDto(List<PlanTask> planTasks) {
        List<PlanTaskResponseDto> assignPlanResponseDtoList = new ArrayList<>();
        for (PlanTask planTask : planTasks) {
            assignPlanResponseDtoList.add(mapAssignPlanToResponseDto(planTask));
        }
        return assignPlanResponseDtoList;
    }
    public PlanTaskResponseDto mapAssignPlanToResponseDto(PlanTask planTask) {
        String planName = null;
        if (planTask.getPlanType() == 1) {
            Course course =  courseService.getCourseById(planTask.getPlan());
            planName = course.getName();
            planTask.setEstimatedTime(course.getEstimatedTime());
        }else if (planTask.getPlanType() == 2) {
            Test test = testService.getTestById(planTask.getPlan());
            planName = test.getTestName();
            planTask.setEstimatedTime(test.getEstimatedTime());
        }
        UserIdAndNameDto planIdAndNameDto = UserIdAndNameDto.builder()
                .name(planName)
                ._id(planTask.getPlan())
                .build();
        return PlanTaskResponseDto.builder()
                ._id(planTask.get_id())
                .plan(planIdAndNameDto)
                .planType(planTask.getPlanType())
                .phases(planTask.getPhases())
                .estimatedTime(planTask.getEstimatedTime())
                .mentor(planTask.getMentorDetails())
                .build();
    }
}
