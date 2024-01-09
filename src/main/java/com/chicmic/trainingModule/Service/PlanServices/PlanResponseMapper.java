package com.chicmic.trainingModule.Service.PlanServices;

import com.chicmic.trainingModule.Dto.PlanDto.PlanResponseDto;
import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.chicmic.trainingModule.Entity.*;
import com.chicmic.trainingModule.Service.CourseServices.CourseService;
import com.chicmic.trainingModule.Service.TestServices.TestService;
import com.chicmic.trainingModule.TrainingModuleApplication;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlanResponseMapper {
    private final TestService testService;
    private final CourseService courseService;

    public List<PlanResponseDto> mapPlanToResponseDto(List<Plan> plans, Boolean isMilestoneRequired) {
        List<PlanResponseDto> planResponseDtoList = new ArrayList<>();
        for (Plan plan : plans) {
            planResponseDtoList.add(mapPlanToResponseDto(plan));
        }
        return planResponseDtoList;
    }

    public PlanResponseDto mapPlanToResponseDto(Plan plan) {
        List<Phase<PlanTask>> phases = plan.getPhases();
        for (Phase<PlanTask> phase : phases) {
            for (PlanTask planTask : phase.getTasks()) {
                if(planTask == null){
                    System.out.println("planTask is null  " + phase.get_id());
                    continue;
                }
                List<Object> milestoneDetails = new ArrayList<>();
                if(planTask.getMilestones() != null) {
                    for (Object milestoneId : planTask.getMilestones()) {
                        UserIdAndNameDto milestoneDetail = null;
                        System.out.println("Milestone Hello Response: " + milestoneId);
                        milestoneDetail = UserIdAndNameDto.builder()
                                .name(courseService.getPhaseById((String)milestoneId).getName())
                                ._id((String) milestoneId)
                                .build();
//                        if (planTask.getPlanType() == 2) {
//                            milestoneDetail = UserIdAndNameDto.builder()
//                                    .name((testService.getTestById(planTask.getPlan()).getTestName()))
//                                    ._id((String) milestoneId)
//                                    .build();
//                        } else if (planTask.getPlanType() == 1) {
//                            milestoneDetail = UserIdAndNameDto.builder()
//                                    .name(courseService.getCourseById(planTask.getPlan()).getName())
//                                    ._id((String) milestoneId)
//                                    .build();
//                        }else if (planTask.getPlanType() == 3) {
//                            milestoneDetail = UserIdAndNameDto.builder()
//                                    .name((courseService.getCourseById(planTask.getPlan()).getName()))
//                                    ._id((String) milestoneId)
//                                    .build();
//                        }
                        milestoneDetails.add(milestoneDetail);
                    }
                }
                if(planTask.getPlanType() == 2) {
                    Test test = testService.getTestById(planTask.getPlan());
                    planTask.setPlanName(test == null ? "Test Not Found" : test.getTestName());
                }else {
                    Course course = courseService.getCourseById(planTask.getPlan());
                    planTask.setPlanName(course == null ? "Course Not Found" : course.getName());
                }
                planTask.setMilestones(milestoneDetails);
            }
        }
        return PlanResponseDto.builder()
                ._id(plan.get_id())
                .planName(plan.getPlanName())
                .description(plan.getDescription())
                .estimatedTime(plan.getEstimatedTime())
                .noOfPhases(plan.getPhases().size())
                .noOfTasks(plan.getTotalTasks())
                .approver(plan.getApproverDetails())
                .totalPhases(plan.getPhases().size())
                .phases(phases)
                .deleted(plan.getDeleted())
                .approvedBy(plan.getApprovedByDetails())
                .approved(plan.getApproved())
                .createdBy(plan.getCreatedBy())
                .createdByName(TrainingModuleApplication.searchNameById(plan.getCreatedBy()))
                .createdAt(plan.getCreatedAt())
                .build();
    }
}
