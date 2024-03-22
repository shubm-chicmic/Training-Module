package com.chicmic.trainingModule.Service.RatingService;

import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.chicmic.trainingModule.Entity.*;
import com.chicmic.trainingModule.Entity.Constants.PlanType;
import com.chicmic.trainingModule.Service.AssignTaskService.AssignTaskService;
import com.chicmic.trainingModule.Service.PhaseService;
import com.chicmic.trainingModule.Service.UserTimeService.UserTimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RatingService {
    private final AssignTaskService assignTaskService;
    private final UserTimeService userTimeService;
    private final PhaseService phaseService;
    public double courseRatingForUserWithOverTimeDeduction(String traineeId){
        AssignedPlan assignedPlan = assignTaskService.getAllAssignTasksByTraineeId(traineeId);
        if(assignedPlan == null || traineeId == null) return 0.0;
        List<Plan> planList = assignedPlan.getPlans();
        double courseRating = 0.0f;
        Integer totalCourse = 0;
        for (Plan plan : planList) {
            for (Phase<PlanTask> planPhase : plan.getPhases()) {
                for (PlanTask planTask : planPhase.getTasks()) {
                    if(planTask != null){
                        Integer consumedTime = 0;
                        if(planTask.getPlanType() == PlanType.COURSE){
                            for (Object milestone : planTask.getMilestones()) {
                                Phase<Task> phase = (Phase<Task>) phaseService.getPhaseById((String) milestone);
                                if (phase != null){
                                    List<Task> tasks = phase.getTasks();
                                    List<SubTask> subTasks = tasks.stream()
                                            .flatMap(task -> task.getSubtasks().stream())
                                            .collect(Collectors.toList());

                                    for (SubTask subTask : subTasks) {
                                        consumedTime += userTimeService.getTotalTimeByTraineeIdAndPlanIdAndPlanTaskIdAndSubTaskId(traineeId, plan.get_id(), planTask.get_id(), subTask.get_id());
                                    }
                                }
                            }

                            if(consumedTime == 0) {
                                continue;
                            }
                            Integer estimatedTime = planTask.getEstimatedTimeInSeconds();
                            Integer totalDeductedRating = 0;
                            if(consumedTime < estimatedTime) {
                                courseRating += 5;
                            }
                            else if(consumedTime == estimatedTime){
                                courseRating += 4;
                            }
                            else if (consumedTime > estimatedTime) {
                                double percentageIncrease = ((double) (consumedTime - estimatedTime) / estimatedTime) * 100;
                                System.out.println("Percentage increase: " + percentageIncrease);
                                int intervals = (int) Math.ceil(percentageIncrease / 10);
                                totalDeductedRating += intervals;

                                totalDeductedRating = totalDeductedRating >= 7 ? 7 : totalDeductedRating;
                                courseRating += 4 - (totalDeductedRating * 0.5);
                            }
                            totalCourse += 1;
                            System.out.println("\u001B[45m");
                            System.out.println("total deduction = " + totalDeductedRating);
                            System.out.println("course rating = " + courseRating);
                            System.out.println("plantask id " + planTask.get_id());
                            System.out.println("\u001B[0m");
                        }

                    }
                }
            }
        }
        if(totalCourse == 0) {
            return 0;
        }
        double finalRating = courseRating / totalCourse;
        finalRating = finalRating <= 0.50 ? 0.50 : finalRating;
        System.out.println("\u001B[43m final rating " + finalRating + " courseRating " + courseRating + " totalcourse "  + totalCourse +  "\u001B[0m");

        return finalRating;
    }
}
