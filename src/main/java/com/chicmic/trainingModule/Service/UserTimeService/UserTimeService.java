package com.chicmic.trainingModule.Service.UserTimeService;

import com.chicmic.trainingModule.Dto.TimeTrack;
import com.chicmic.trainingModule.Dto.UserTimeDto.UserTimeDto;
import com.chicmic.trainingModule.Entity.*;
import com.chicmic.trainingModule.Entity.Constants.PlanType;
import com.chicmic.trainingModule.Entity.Constants.TimeSheetType;
import com.chicmic.trainingModule.Repository.UserTimeRepo;
import com.chicmic.trainingModule.Service.AssignTaskService.AssignTaskService;
import com.chicmic.trainingModule.Service.PhaseService;
import com.chicmic.trainingModule.Service.PlanServices.PlanTaskService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserTimeService {
    private final UserTimeRepo userTimeRepo;
    private final PhaseService phaseService;
    private final PlanTaskService planTaskService;
    private final AssignTaskService assignTaskService;
    public UserTimeService(UserTimeRepo userTimeRepo, PhaseService phaseService, PlanTaskService planTaskService, AssignTaskService assignTaskService) {
        this.userTimeRepo = userTimeRepo;
        this.phaseService = phaseService;
        this.planTaskService = planTaskService;
        this.assignTaskService = assignTaskService;
    }
    public List<UserTime> getUserTimeByTraineeId(String traineeId) {
        return userTimeRepo.findByTraineeId(traineeId, TimeSheetType.VIVA, TimeSheetType.PPT);
    }
    public List<String> getUniqueTraineeIds() {
        List<UserTime> userTimes = userTimeRepo.findDistinctTraineeIds();
        return userTimes.stream()
                .map(UserTime::getTraineeId)
                .distinct()
                .collect(Collectors.toList());
    }
    public UserTime getUserTimeByDto(UserTimeDto userTimeDto, String traineeId) {
        if (userTimeDto == null) {
            return null;
        }
        if(userTimeDto.getType() != TimeSheetType.VIVA && userTimeDto.getType() != TimeSheetType.PPT) {
            // Retrieve user time based on the provided parameters
            List<UserTime> userTimeList =  userTimeRepo.findByTraineeIdAndPlanIdAndPlanTaskIdAndSubTaskId(
                    traineeId,
                    userTimeDto.getPlanId(),
                    userTimeDto.getPlanTaskId(),
                    userTimeDto.getSubTaskId(), TimeSheetType.VIVA, TimeSheetType.PPT
            );
            System.out.println("Getting userTime size " + userTimeList.size());
            return userTimeList != null && userTimeList.size() > 0 ? userTimeList.get(0) : null;
        }else {
            return userTimeRepo.findByTraineeIdAndPlanIdAndPlanTaskIdForVivaAndPPT(traineeId, userTimeDto.getPlanId(), userTimeDto.getPlanTaskId(), TimeSheetType.VIVA, TimeSheetType.PPT);
        }
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

    public UserTime saveUserTime(UserTime userTime) {
        return userTimeRepo.save(userTime);
    }

    public UserTime createUserTime(UserTimeDto userTimeDto, String traineeId) {

        SubTask subTask = null;
        PlanTask planTask = null;
        if(userTimeDto.getType() != PlanType.PPT && userTimeDto.getType() != PlanType.VIVA){
            subTask = phaseService.getSubTaskById(userTimeDto.getSubTaskId());
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
                planTask = planTaskService.findByTypeAndPlanAndMilestoneIdForCourseAndTest(userTimeDto.getType(),moduleId , phase.get_id(), userTimeDto.getPlanId());
            }
        }else {
            String planTaskId = userTimeDto.getTaskId();
            planTask = planTaskService.getPlanTaskById(planTaskId);
        }
        if(planTask != null)
        userTimeDto.setPlanTaskId(planTask.get_id());
        UserTime existingUserTime = getUserTimeByDto(userTimeDto, traineeId);
        if (existingUserTime != null) {
//            // Update the existing user time
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
                .consumedTime(userTimeDto.getConsumedTime() < 0 ? 0 : userTimeDto.getConsumedTime())
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
                .consumedTime(userTimeDto.getConsumedTime() < 0 ? 0 : userTimeDto.getConsumedTime())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return saveUserTime(userTime);
    }

    public Integer calculateConsumedTimeInPlanTask(String traineeId, Plan plan, PlanTask planTask) {
        List<Object> milestones = planTask.getMilestones();
        Integer consumedTime = 0;
        for (Object milestone : milestones) {
            Phase<Task> phase = (Phase<Task>) phaseService.getPhaseById((String) milestone);
            for (Task task : phase.getTasks()) {
                for (SubTask subTask : task.getSubtasks()) {
                    List<UserTime> userTime = userTimeRepo.findByTraineeIdAndPlanIdAndPlanTaskIdAndSubTaskId(traineeId, plan.get_id(), planTask.get_id(), subTask.get_id(), TimeSheetType.VIVA, TimeSheetType.PPT);
                    for(UserTime subtaskTime : userTime) {
                        consumedTime += subtaskTime.getConsumedTime();
                    }
                }
            }
        }
        return consumedTime;
    }

    public Integer getExtraConsumedTimeForPlanTask(String planTaskId, String traineeId, Plan plan) {
        PlanTask planTask = planTaskService.getPlanTaskById(planTaskId);
        if(planTask == null)return 0;
        if(planTask.getPlanType() == PlanType.COURSE) {
            Integer consumedTime = 0;
            for (Object milestone : planTask.getMilestones()) {
                Phase<Task> phase = (Phase<Task>) phaseService.getPhaseById((String) milestone);
                if (phase != null) {
                    List<Task> tasks = phase.getTasks();
                    List<SubTask> subTasks = tasks.stream()
                            .flatMap(task -> task.getSubtasks().stream())
                            .collect(Collectors.toList());

                    for (SubTask subTask : subTasks) {
                        consumedTime += getTotalTimeByTraineeIdAndPlanIdAndPlanTaskIdAndSubTaskId(traineeId, plan.get_id(), planTask.get_id(), subTask.get_id());
                    }
                }
            }
            Integer estimatedTime = planTask.getEstimatedTimeInSeconds();
            return  consumedTime >= estimatedTime ? consumedTime - estimatedTime : 0;
        }
        return 0;
    }
    public TimeTrack getTimeForPlanTask(String planTaskId, String traineeId, Plan plan) {
        PlanTask planTask = planTaskService.getPlanTaskById(planTaskId);
        if(planTask == null)return null;
        if(planTask.getPlanType() == PlanType.COURSE || planTask.getPlanType() == PlanType.TEST) {
            Integer consumedTime = 0;
            for (Object milestone : planTask.getMilestones()) {
                Phase<Task> phase = (Phase<Task>) phaseService.getPhaseById((String) milestone);
                if (phase != null) {
                    List<Task> tasks = phase.getTasks();
                    List<SubTask> subTasks = tasks.stream()
                            .flatMap(task -> task.getSubtasks().stream())
                            .collect(Collectors.toList());

                    for (SubTask subTask : subTasks) {
                        consumedTime += getTotalTimeByTraineeIdAndPlanIdAndPlanTaskIdAndSubTaskId(traineeId, plan.get_id(), planTask.get_id(), subTask.get_id());
                    }
                }
            }
            Integer estimatedTime = planTask.getEstimatedTimeInSeconds();
            return TimeTrack.builder()
                    .estimatedTime(estimatedTime)
                    .consumedTime(consumedTime)
                    .build();
//            return  consumedTime >= estimatedTime ? consumedTime - estimatedTime : 0;
        }
        return null;
    }
    public TimeTrack getTimeForCourseInsidePlan(String courseId, String planId, String traineeId) {
        AssignedPlan assignedPlan = assignTaskService.getAllAssignTasksByTraineeId(traineeId);
        List<Plan> plans = assignedPlan.getPlans();
        Integer estimatedTime = 0;
        Integer consumedTime = 0;
        for (Plan plan : plans) {
            if(plan.get_id().equals(planId)){
                for (Phase<PlanTask> phase : plan.getPhases()) {
                    for (PlanTask planTask : phase.getTasks()) {
                        if(planTask.getPlanType() == PlanType.COURSE && planTask.getPlan().equals(courseId)){
                            for (Object milestone : planTask.getMilestones()) {
                                Phase<Task> coursePhase = (Phase<Task>) phaseService.getPhaseById((String) milestone);
                                if (coursePhase != null) {
                                    List<Task> tasks = coursePhase.getTasks();
                                    List<SubTask> subTasks = tasks.stream()
                                            .flatMap(task -> task.getSubtasks().stream())
                                            .collect(Collectors.toList());

                                    for (SubTask subTask : subTasks) {
                                        consumedTime += getTotalTimeByTraineeIdAndPlanIdAndPlanTaskIdAndSubTaskId(traineeId, plan.get_id(), planTask.get_id(), subTask.get_id());
                                    }
                                }
                            }
                            estimatedTime += planTask.getEstimatedTimeInSeconds();
                        }
                    }
                }
            }
        }
        return TimeTrack.builder()
                .consumedTime(consumedTime)
                .estimatedTime(estimatedTime)
                .build();
    }
}
