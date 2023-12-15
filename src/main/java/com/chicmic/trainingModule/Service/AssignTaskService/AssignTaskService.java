package com.chicmic.trainingModule.Service.AssignTaskService;

import com.chicmic.trainingModule.Dto.AssignTaskDto;
import com.chicmic.trainingModule.Entity.AssignTask.AssignTask;
import com.chicmic.trainingModule.Entity.Plan.Plan;
import com.chicmic.trainingModule.Repository.AssignTaskRepo;
import com.chicmic.trainingModule.Repository.CourseRepo;
import com.chicmic.trainingModule.Service.PlanServices.PlanService;
import com.chicmic.trainingModule.Util.CustomObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AssignTaskService {
    private final AssignTaskRepo assignTaskRepo;
    private final PlanService planService;
    private final MongoTemplate mongoTemplate;
    public AssignTask createAssignTask(AssignTaskDto assignTaskDto, Principal principal) {
        AssignTask assignTask = CustomObjectMapper.convert(assignTaskDto, AssignTask.class);
        assignTask.setCreatedBy(principal.getName());
        List<Plan> plan = planService.getPlanByIds(assignTaskDto.getPlan());
        assignTask.setPlans(plan);
        return assignTaskRepo.save(assignTask);
    }
}
