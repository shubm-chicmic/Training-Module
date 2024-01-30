package com.chicmic.trainingModule.Service.UserTimeService;

import com.chicmic.trainingModule.Dto.UserTimeDto.UserTimeDto;
import com.chicmic.trainingModule.Entity.*;
import com.chicmic.trainingModule.Entity.Constants.PlanType;
import com.chicmic.trainingModule.Repository.UserTimeRepo;
import com.chicmic.trainingModule.Service.PhaseService;
import com.chicmic.trainingModule.Service.PlanServices.PlanTaskService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserTimeService {
    private final UserTimeRepo userTimeRepo;
    private final PhaseService phaseService;
    private final PlanTaskService planTaskService;

    public UserTimeService(UserTimeRepo userTimeRepo, PhaseService phaseService, PlanTaskService planTaskService) {
        this.userTimeRepo = userTimeRepo;
        this.phaseService = phaseService;
        this.planTaskService = planTaskService;
    }

    public UserTime saveUserTime(UserTime userTime) {
        return userTimeRepo.save(userTime);
    }

    public UserTime createUserTime(UserTimeDto userTimeDto, String traineeId) {
        SubTask subTask = null;
        PlanTask planTask = null;
        if(userTimeDto.getType() != PlanType.PPT && userTimeDto.getType() != PlanType.VIVA){
            subTask = phaseService.getSubTaskById(userTimeDto.getSubTaskId());
        }else {

        }
        if(subTask != null) {
            Phase<Task> phase = subTask.getPhase();
            String moduleId = null;
            if(phase.getEntityType() == PlanType.COURSE){
                Course course = ((Course)phase.getEntity());
                if(course != null){
                    moduleId = course.get_id();
                }
            }else if(phase.getEntityType() == PlanType.TEST){
                Test test = ((Test)phase.getEntity());
                if(test != null){
                    moduleId = test.get_id();
                }
            }
            planTask = planTaskService.findByTypeAndPlanAndMilestoneIdForCourseAndTest(userTimeDto.getType(),moduleId , phase.get_id());
        }
        UserTime userTime = UserTime.builder()
                .traineeId(traineeId)
                .consumedTime(userTimeDto.getConsumedTime())
                .isDeleted(false)
                .planId(userTimeDto.getPlanId())
                .planTaskId(planTask != null ? planTask.get_id() : null)
                .subTaskId(userTimeDto.getSubTaskId())
                .build();
        return saveUserTime(userTime);
    }
}
