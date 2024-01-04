package com.chicmic.trainingModule.Service.AssignTaskService;


import com.chicmic.trainingModule.Dto.AssignTaskDto.PlanTaskResponseDto;
import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.chicmic.trainingModule.Entity.*;
import com.chicmic.trainingModule.Service.CourseServices.CourseService;
import com.chicmic.trainingModule.Service.TestServices.TestService;
import com.chicmic.trainingModule.Service.UserProgressService.UserProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AssignPlanResponseMapper {
    private final TestService testService;
    private final CourseService courseService;
    private final UserProgressService userProgressService;

    public List<PlanTaskResponseDto> mapAssignPlanToResponseDto(List<PlanTask> planTasks,String planId, String traineeId) {
        List<PlanTaskResponseDto> assignPlanResponseDtoList = new ArrayList<>();
        for (PlanTask planTask : planTasks) {
            assignPlanResponseDtoList.add(mapAssignPlanToResponseDto(planTask, planId, traineeId));
        }
        return assignPlanResponseDtoList;
    }
    public PlanTaskResponseDto mapAssignPlanToResponseDto(PlanTask planTask,String planId, String traineeId) {
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
        Boolean isPlanCompleted = userProgressService.findIsPlanCompleted(planId,planTask.getPlan(), planTask.getPlanType(), traineeId);

        List<UserIdAndNameDto> milestonesIdAndName = new ArrayList<>();
        for (Object milestone : planTask.getMilestones()){
            Phase<Task> phase = courseService.getPhaseById((String) milestone);
            UserIdAndNameDto milestoneDetails = UserIdAndNameDto.builder()
                    ._id(phase.get_id())
                    .name(phase.getName())
                    .build();
            milestonesIdAndName.add(milestoneDetails);
        }
        Integer completedTask = userProgressService.getTotalSubTaskCompleted(traineeId,planId,planTask.getPlan(),5);
        return PlanTaskResponseDto.builder()
                ._id(planTask.get_id())
                .plan(planIdAndNameDto)
                .planType(planTask.getPlanType())
                .phases(milestonesIdAndName)
                .consumedTime("00:00")
                .completedTask(completedTask)
                .totalTasks(1)
                .estimatedTime(planTask.getEstimatedTime())
                .mentor(planTask.getMentorDetails())
                .isCompleted(isPlanCompleted)
                .rating(0f)
                .build();
    }
}
