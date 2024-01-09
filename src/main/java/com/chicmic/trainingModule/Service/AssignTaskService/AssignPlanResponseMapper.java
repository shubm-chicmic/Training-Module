package com.chicmic.trainingModule.Service.AssignTaskService;


import com.chicmic.trainingModule.Dto.AssignTaskDto.PlanTaskResponseDto;
import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.chicmic.trainingModule.Dto.UserProgressDto;
import com.chicmic.trainingModule.Entity.*;
import com.chicmic.trainingModule.Entity.Constants.EntityType;
import com.chicmic.trainingModule.Entity.Constants.ProgessConstants;
import com.chicmic.trainingModule.Service.CourseServices.CourseService;
import com.chicmic.trainingModule.Service.FeedBackService.FeedbackProgressService;
import com.chicmic.trainingModule.Service.FeedBackService.FeedbackService_V2;
import com.chicmic.trainingModule.Service.PhaseService;
import com.chicmic.trainingModule.Service.TestServices.TestService;
import com.chicmic.trainingModule.Service.UserProgressService.UserProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssignPlanResponseMapper {
    private final TestService testService;
    private final CourseService courseService;
    private final PhaseService phaseService;
    private final UserProgressService userProgressService;
    private final FeedbackProgressService feedbackServiceV2;

    public List<PlanTaskResponseDto> mapAssignPlanToResponseDto(List<PlanTask> planTasks,String planId, String traineeId) {
        System.out.println("planTasks: " + planTasks.size());
        List<PlanTaskResponseDto> assignPlanResponseDtoList = new ArrayList<>();
        for (PlanTask planTask : planTasks) {
            assignPlanResponseDtoList.add(mapAssignPlanToResponseDto(planTask, planId, traineeId));
        }
        System.out.println("assignPlanResponseDtoList: " + assignPlanResponseDtoList.size());
        return assignPlanResponseDtoList;
    }
    public PlanTaskResponseDto mapAssignPlanToResponseDto(PlanTask planTask,String planId, String traineeId) {
        String planName = null;
        if(planTask == null) {
            return null;
        }
        if (planTask.getPlanType() == 1) {
            Course course =  courseService.getCourseById(planTask.getPlan());
            if(course != null) {
                planName = course.getName();
            }
//            planTask.setEstimatedTime(course.getEstimatedTime());
        }else if (planTask.getPlanType() == 2) {
            Test test = testService.getTestById(planTask.getPlan());
            if(test != null) {
                planName = test.getTestName();
            }
//            planTask.setEstimatedTime(test.getEstimatedTime());
        }else {
            Course course =  courseService.getCourseById(planTask.getPlan());
            if(course != null) {
                planName = course.getName();
            }
//            planTask.setEstimatedTime(course.getEstimatedTime());
        }
        UserIdAndNameDto planIdAndNameDto = UserIdAndNameDto.builder()
                .name(planName)
                ._id(planTask.getPlan())
                .build();

        List<UserIdAndNameDto> milestonesIdAndName = new ArrayList<>();
        Integer totalTask = 0;
        Integer completedTasks = 0;
        if(planTask.getMilestones() == null){
            planTask.setMilestones(new ArrayList<>());
        }
        for (Object milestone : planTask.getMilestones()){
            Phase<Task> phase = phaseService.getPhaseById((String) milestone);
            List<Task> tasks = phase.getTasks();
            List<SubTask> subTasks = tasks.stream()
                    .flatMap(task -> task.getSubtasks().stream())
                    .collect(Collectors.toList());

            for (SubTask subTask : subTasks) {
                if(userProgressService.findIsSubTaskCompleted(planId, planTask.getPlan(), subTask.get_id(),traineeId)){
                    completedTasks++;
                }
            }
            UserIdAndNameDto milestoneDetails = UserIdAndNameDto.builder()
                    ._id(phase.get_id())
                    .name(phase.getName())
                    .build();
            milestonesIdAndName.add(milestoneDetails);
            totalTask += phase.getTotalTasks();
        }
//        Integer completedTasks = userProgressService.getTotalSubTaskCompleted(traineeId,planId,planTask.getPlan(),5);
        Boolean isPlanCompleted = false;
        if(totalTask == completedTasks) {
            UserProgress planProgress = userProgressService.getUserProgressByTraineeIdPlanIdAndCourseId(traineeId, planId, planTask.getPlan(), EntityType.COURSE);
            if(planProgress == null) {
                UserProgress userProgress = UserProgress.builder()
                        .planId(planId)
                        .courseId(planTask.getPlan())
                        .progressType(planTask.getPlanType())
                        .status(ProgessConstants.Completed)
                        .build();
                userProgressService.createUserProgress(userProgress);
            }else {
                planProgress.setStatus(ProgessConstants.Completed);
                userProgressService.createUserProgress(planProgress);
            }
            if(planProgress != null) {
                isPlanCompleted = planProgress.getStatus() == ProgessConstants.Completed;
            }
        }


        Integer feedbackType = null;
        if(planTask.getPlanType() == 1) {
            feedbackType = 3;
        }else if (planTask.getPlanType() == 2) {
            feedbackType = 2;
        }else if (planTask.getPlanType() == 3) {
            feedbackType = 3;
        }else if (planTask.getPlanType() == 4){
            feedbackType = 4;
        }
        List<String> milestonesIds = planTask.getMilestones().stream()
                .map(Object::toString)
                .collect(Collectors.toList());
        Feedback_V2 feedbackV2 = feedbackServiceV2.feedbackOfParticularPhaseOfTrainee(traineeId, planTask.getPlan(), milestonesIds, String.valueOf(feedbackType));
        if(planTask.getPlanType() == 3 || planTask.getPlanType() == 4) {
            UserProgress userProgress = userProgressService.getUserProgressByTraineeIdPlanIdAndCourseId(traineeId, planId, planTask.getPlan(), planTask.getPlanType());
            if(userProgress != null) {
                completedTasks = (userProgress.getStatus() == ProgessConstants.Completed) ? 1 : 0;
            }else {
                completedTasks = 0;
            }
            totalTask = 1;
            isPlanCompleted =(totalTask == completedTasks);
        }

        //TODO plantask estimated time is pending 51 hours
        return PlanTaskResponseDto.builder()
                ._id(planTask.get_id())
                .plan(planIdAndNameDto)
                .planType(planTask.getPlanType())
                .phases(milestonesIdAndName)
                .consumedTime("00:00")
                .completedTasks(completedTasks)
                .totalTasks(totalTask)
                .date(planTask.getDate())
                .estimatedTime(planTask.getEstimatedTime())
                .mentor(planTask.getMentorDetails())
                .isCompleted(isPlanCompleted)
                .feedbackId(null)
                .rating(feedbackV2 == null ? 0f : feedbackV2.getOverallRating())
                .build();
    }
}
