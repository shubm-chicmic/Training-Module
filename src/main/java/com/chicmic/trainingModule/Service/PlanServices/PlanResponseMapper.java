package com.chicmic.trainingModule.Service.PlanServices;

import com.chicmic.trainingModule.Dto.PlanDto.PlanResponseDto;
import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.chicmic.trainingModule.Entity.Phase;
import com.chicmic.trainingModule.Entity.Plan;
import com.chicmic.trainingModule.Entity.PlanTask;
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
                List<Object> milestoneDetails = new ArrayList<>();
                for (Object milestoneId : planTask.getMilestones()){
                    UserIdAndNameDto milestoneDetail = null;
                    System.out.println("Milestone : " + milestoneId);
                    if(planTask.getPlanType() == 2){
                        milestoneDetail = UserIdAndNameDto.builder()
                                .name((testService.getTestById(planTask.getPlan()).getTestName()))
                                ._id((String) milestoneId)
                                .build();
                    }else if(planTask.getPlanType() == 1){
                        milestoneDetail = UserIdAndNameDto.builder()
                                .name(courseService.getCourseById(planTask.getPlan()).getName())
                                ._id((String) milestoneId)
                                .build();
                    }
                    milestoneDetails.add(milestoneDetail);
                }
                planTask.setMilestones(milestoneDetails);
            }
        }
        return PlanResponseDto.builder()
                ._id(plan.get_id())
                .planName(plan.getPlanName())
                .description(plan.getDescription())
                .estimatedTime("00:00")
                .noOfPhases(plan.getPhases().size())
                .noOfTasks(0)
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
