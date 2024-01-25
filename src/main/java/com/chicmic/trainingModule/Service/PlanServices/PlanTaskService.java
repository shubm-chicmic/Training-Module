package com.chicmic.trainingModule.Service.PlanServices;

import com.chicmic.trainingModule.Entity.PlanTask;
import com.chicmic.trainingModule.Repository.PlanTaskRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanTaskService {
    private final PlanTaskRepo planTaskRepo;
    public PlanTask getPlanTaskById(String planTaskId) {
        return planTaskRepo.findById(planTaskId).orElse(null);
    }

    public List<PlanTask> findByMilestoneId(String milestoneId) {
        return planTaskRepo.findByMilestoneId(milestoneId);
    }
}
