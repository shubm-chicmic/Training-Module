package com.chicmic.trainingModule.Service.PlanServices;

import com.chicmic.trainingModule.Entity.Constants.PlanType;
import com.chicmic.trainingModule.Entity.PlanTask;
import com.chicmic.trainingModule.Repository.PlanTaskRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanTaskService {
    private final PlanTaskRepo planTaskRepo;
    public PlanTask getPlanTaskById(String taskId) {
        PlanTask task = planTaskRepo.findById(taskId).orElse(null);
        return task.getIsDeleted() ? null : task;
    }

    public List<PlanTask> findByMilestoneId(String milestoneId) {
        return planTaskRepo.findByMilestoneId(milestoneId);
    }
    public PlanTask findByTypeAndPlanAndMilestoneIdForCourseAndTest(Integer type, String plan, String milestoneId, String planId) {
        // Validate type (1, 2, 3, 4)
        if (type < 1 || type > 4) {
            throw new IllegalArgumentException("Invalid type. Type should be 1, 2, 3, or 4.");
        }

        // Check if the type is 1 or 2 to make the task unique
        if (type == PlanType.COURSE || type == PlanType.TEST) {
            List<PlanTask> planTasks = planTaskRepo.findByTypeAndPlanAndMilestoneId(type, plan, milestoneId);
            System.out.println("Plantask size  " + planTasks.size());
            System.out.println("PlanTask  " + planTasks);
            for (PlanTask planTask : planTasks) {
                if(planTask.getPlans() != null && !planTask.getIsDeleted() && planTask.getPlans().get_id().equals(planId)){
                    return planTask;
                }
            }
        }
        return null;
    }
    public List<PlanTask> findPlanTasksByMentorId(String userId) {
        List<PlanTask> planTaskIds = new ArrayList<>();
        List<PlanTask> allPlanTasks = planTaskRepo.findAll();
        if(allPlanTasks == null){
            return null;
        }
        for (PlanTask planTask : allPlanTasks) {
            if(planTask != null) {
                if (planTask.getMentor() != null && planTask.getMentor().contains(userId)) {
                    planTaskIds.add(planTask);
                }
            }
        }

        return planTaskIds;
    }
}
