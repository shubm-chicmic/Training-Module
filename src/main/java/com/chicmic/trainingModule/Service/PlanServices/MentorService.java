package com.chicmic.trainingModule.Service.PlanServices;

import com.chicmic.trainingModule.Entity.Phase;
import com.chicmic.trainingModule.Entity.Plan;
import com.chicmic.trainingModule.Entity.PlanTask;
import com.chicmic.trainingModule.ExceptionHandling.ApiException;
import com.chicmic.trainingModule.Repository.PhaseRepo;
import com.chicmic.trainingModule.Repository.PlanRepo;
import com.chicmic.trainingModule.Repository.PlanTaskRepo;
import com.chicmic.trainingModule.Service.CourseServices.CourseService;
import com.chicmic.trainingModule.Service.PhaseService;
import com.chicmic.trainingModule.Service.TestServices.TestService;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Service
@RequiredArgsConstructor
public class MentorService {
    private final PlanRepo planRepo;
    private final PlanTaskRepo planTaskRepo;
    private final MongoTemplate mongoTemplate;
    public Boolean isUserIsMentorInPlanTask(String userId) {
        List<PlanTask> allPlanTasks = planTaskRepo.findAll();
        if(allPlanTasks == null){
            return null;
        }
        for (PlanTask planTask : allPlanTasks) {
            if(planTask != null) {
                if (planTask.getMentor() != null && planTask.getMentor().contains(userId)) {
                    return true;
                }
            }
        }

        return false;
    }
    public List<Plan> getPlanOfMentor(String mentorId) {
        List<Plan> allPlans = planRepo.findAll();
        return allPlans.stream()
                .filter(plan -> plan.getPhases() != null)
                .filter(plan -> plan.getPhases().stream()
                        .flatMap(phase -> phase.getTasks().stream())
//                        .peek(task -> System.out.println("\u001B[45m Task: " + task + "\u001B[0m"))
                        .anyMatch(task -> task.getMentor() != null && task.getMentorIds().contains(mentorId)))
                .collect(Collectors.toList());
    }
    public List<PlanTask> getPlanTasksOfMentor(String mentorId) {
        List<PlanTask> planTasks = new ArrayList<>();
        List<PlanTask> allPlanTasks = planTaskRepo.findAll();
        for (PlanTask planTask : allPlanTasks) {
            if(planTask != null) {
                if (planTask.getMentor() != null && planTask.getMentor().contains(mentorId)) {
                    planTasks.add(planTask);
                }
            }
        }
        return planTasks;
    }

    public Boolean isMentorInPlan(String mentorId, String planId) {
        Plan plan = planRepo.findById(planId).orElse(null);
        if(plan == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Plan Id is Invalid");
        }
        for (Phase<PlanTask> phase : plan.getPhases()){
            for (PlanTask planTask : phase.getTasks()) {
                if (planTask.getMentor() != null && planTask.getMentor().contains(mentorId)) {
                    return true;
                }
            }
        }
        return false;
    }

    public Boolean isMentorInPlanTask(String mentorId, String planTaskId){
        PlanTask planTask = planTaskRepo.findById(planTaskId).orElse(null);
        if(planTask == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "PlanTask Id is Invalid");
        }
        for (String planTaskMentorIds : planTask.getMentorIds()) {
            if(planTaskMentorIds.equals(mentorId)){
                return true;
            }
        }
       return false;
    }

}
