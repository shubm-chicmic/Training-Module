package com.chicmic.trainingModule.Service.UserTimeService;

import com.chicmic.trainingModule.Dto.UserTimeDto.UserTimeDto;
import com.chicmic.trainingModule.Entity.*;
import com.chicmic.trainingModule.Entity.Constants.PlanType;
import com.chicmic.trainingModule.Repository.UserTimeRepo;
import com.chicmic.trainingModule.Service.PhaseService;
import com.chicmic.trainingModule.Service.PlanServices.PlanTaskService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserTimeService {
    private final UserTimeRepo userTimeRepo;
    private final PhaseService phaseService;
    private final PlanTaskService planTaskService;
    public UserTime getUserTimeByDto(UserTimeDto userTimeDto) {
        if (userTimeDto == null) {
            // Handle the case where the DTO is null
            return null;
        }

        // Retrieve user time based on the provided parameters
        return userTimeRepo.findByPlanIdAndPlanTaskIdAndSubTaskId(
                userTimeDto.getPlanId(),
                userTimeDto.getPlanTaskId(),
                userTimeDto.getSubTaskId()
        ).orElse(null);
    }
    public UserTime getSessionByDto(UserTimeDto userTimeDto) {
        if (userTimeDto == null) {
            return null;
        }

        // Retrieve user time based on the provided parameters
        return userTimeRepo.findBySessionIdAndType(
                userTimeDto.getTaskId(),
                userTimeDto.getType()
        ).orElse(null);
    }
    public Integer getTotalTimeByTraineeId(String traineeId) {
        List<UserTime> userTimes = userTimeRepo.findByTraineeId(traineeId, PlanType.VIVA, PlanType.PPT);
        return calculateTotalTime(userTimes);
    }

    // 2. Find the total time of a plan for a particular trainee
    public Integer getTotalTimeByTraineeIdAndPlanId(String traineeId, String planId) {
        List<UserTime> userTimes = userTimeRepo.findByTraineeIdAndPlanId(traineeId, planId, PlanType.VIVA, PlanType.PPT);
        return calculateTotalTime(userTimes);
    }

    // 3. Find total time for a plan task
    public Integer getTotalTimeByTraineeIdAndPlanIdAndPlanTaskId(String traineeId, String planId, String planTaskId) {
        List<UserTime> userTimes = userTimeRepo.findByTraineeIdAndPlanIdAndPlanTaskId(traineeId, planId, planTaskId, PlanType.VIVA, PlanType.PPT);
        System.out.println("\u001B[45m Total time for a plan task: " + userTimes.size() + "\u001B[0m");
        System.out.println("trainee ID : " + traineeId);
        System.out.println("plan ID : " + planId);
        System.out.println("plan task ID : " + planTaskId);
        return calculateTotalTime(userTimes);
    }

    // 4. Find total time for a plan task and subtask
    public Integer getTotalTimeByTraineeIdAndPlanIdAndPlanTaskIdAndSubTaskId(String traineeId, String planId, String planTaskId, String subTaskId) {
        List<UserTime> userTimes = userTimeRepo.findByTraineeIdAndPlanIdAndPlanTaskIdAndSubTaskId(traineeId, planId, planTaskId, subTaskId, PlanType.VIVA, PlanType.PPT);
        return calculateTotalTime(userTimes);
    }

    // Helper method to calculate total time from a list of UserTime objects
    private Integer calculateTotalTime(List<UserTime> userTimes) {
        return userTimes.stream()
                .map(UserTime::getConsumedTime)
                .reduce(0, Integer::sum);
    }
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
            String planTaskId = userTimeDto.getTaskId();
            planTask = planTaskService.getPlanTaskById(planTaskId);
        }
        if(subTask != null) {
            Task task = subTask.getTask();
            Phase<Task> phase = task.getPhase();
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
            planTask = planTaskService. findByTypeAndPlanAndMilestoneIdForCourseAndTest(userTimeDto.getType(),moduleId , phase.get_id(), userTimeDto.getPlanId());
        }
        userTimeDto.setPlanTaskId(planTask.get_id());
        UserTime existingUserTime = getUserTimeByDto(userTimeDto);
        if (existingUserTime != null) {
            // Update the existing user time
            Integer finalEstimateTime = existingUserTime.getConsumedTime() + userTimeDto.getConsumedTime();
            if(finalEstimateTime < 0)finalEstimateTime = 0;
            existingUserTime.setConsumedTime(finalEstimateTime);
            existingUserTime.setUpdatedAt(LocalDateTime.now());
            // You may need to update other fields as well if necessary
            return saveUserTime(existingUserTime); // Save the updated user time
        }
        UserTime userTime = UserTime.builder()
                .traineeId(traineeId)
                .type(userTimeDto.getType())
                .consumedTime(userTimeDto.getConsumedTime())
                .isDeleted(false)
                .planId(userTimeDto.getPlanId())
                .planTaskId(planTask != null ? planTask.get_id() : null)
                .subTaskId(userTimeDto.getSubTaskId())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return saveUserTime(userTime);
    }

    public UserTime createSessionTimeForUser(UserTimeDto userTimeDto, String userId) {
        UserTime existingUserTime = getSessionByDto(userTimeDto);
        if (existingUserTime != null) {
            // Update the existing user time
            Integer finalEstimateTime = existingUserTime.getConsumedTime() + userTimeDto.getConsumedTime();
            if(finalEstimateTime < 0)finalEstimateTime = 0;
            existingUserTime.setConsumedTime(finalEstimateTime);
            existingUserTime.setUpdatedAt(LocalDateTime.now());
            // You may need to update other fields as well if necessary
            return saveUserTime(existingUserTime); // Save the updated user time
        }
        UserTime userTime = UserTime.builder()
                .traineeId(userId)
                .type(userTimeDto.getType())
                .sessionId(userTimeDto.getTaskId())
                .consumedTime(userTimeDto.getConsumedTime())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return saveUserTime(userTime);
    }
}
