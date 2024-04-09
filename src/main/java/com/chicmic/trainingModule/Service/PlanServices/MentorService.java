package com.chicmic.trainingModule.Service.PlanServices;

import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponseWithCount;
import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.chicmic.trainingModule.Entity.AssignedPlan;
import com.chicmic.trainingModule.Entity.Phase;
import com.chicmic.trainingModule.Entity.Plan;
import com.chicmic.trainingModule.Entity.PlanTask;
import com.chicmic.trainingModule.ExceptionHandling.ApiException;
import com.chicmic.trainingModule.Repository.PlanRepo;
import com.chicmic.trainingModule.Repository.PlanTaskRepo;
import com.chicmic.trainingModule.Service.AssignTaskService.AssignTaskService;
import com.chicmic.trainingModule.Service.FeedBackService.MentorFeedbackService;
import com.chicmic.trainingModule.Util.Pagenation;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MentorService {
    private final PlanRepo planRepo;
    private final PlanTaskRepo planTaskRepo;
    private final AssignTaskService assignTaskService;
    private final MentorFeedbackService mentorFeedbackService;
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
    public Boolean isUserAMentorOfTrainee(String userId, String traineeId) {
        AssignedPlan assignedPlan = assignTaskService.getAllAssignTasksByTraineeId(traineeId);
        List<Plan> plans = assignedPlan.getPlans();
        for (Plan plan : plans) {
            for (Phase<PlanTask> phase : plan.getPhases()) {
                for (PlanTask planTask : phase.getTasks()) {
                    if(planTask != null) {
                        if(planTask.getMentorIds() != null && planTask.getMentorIds().contains(userId)){
                            return true;
                        }
                    }
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
    public List<UserIdAndNameDto> getMentorOfTrainee(String traineeId){
        System.out.println("trianee id is " + traineeId);
        AssignedPlan assignedPlan = assignTaskService.getAllAssignTasksByTraineeId(traineeId);
        if(assignedPlan == null) return new ArrayList<>();
        List<UserIdAndNameDto> mentorList = new ArrayList<>();
        List<Plan> plans = assignedPlan.getPlans();
        Set<String> uniqueMentorIds = new HashSet<>();
        for (Plan plan : plans) {
            for (Phase<PlanTask> phase : plan.getPhases()) {
                for (PlanTask planTask : phase.getTasks()) {
                    if(planTask != null) {
                        if(planTask.getMentorIds() != null && planTask.getMentorDetails() != null) {
                            for (UserIdAndNameDto mentorDetail : planTask.getMentorDetails()) {
                                // Check if the mentor ID has not been encountered before
                                if (!uniqueMentorIds.contains(mentorDetail.get_id())) {
                                    mentorList.add(mentorDetail); // Add mentor details to the list
                                    uniqueMentorIds.add(mentorDetail.get_id()); // Add mentor ID to the HashSet
                                }
                            }
                        }
                    }
                }
            }
        }
        return mentorList;
    }
    public ApiResponseWithCount getAllMentors(Integer pageNumber, Integer pageSize, Integer sortDirection, String sortKey, String searchString) {
        List<PlanTask> allPlanTasks = planTaskRepo.findAll();
        if(allPlanTasks == null){
            return null;
        }
        List<UserIdAndNameDto> mentorDetails = new ArrayList<>();
        Set<String> uniqueMentorIds = new HashSet<>();
        for (PlanTask planTask : allPlanTasks) {
            if(planTask != null) {
                if(planTask.getMentorIds() != null && planTask.getMentorDetails() != null) {
                    for (UserIdAndNameDto mentorDetail : planTask.getMentorDetails()) {
                        // Check if the mentor ID has not been encountered before
                        if (!uniqueMentorIds.contains(mentorDetail.get_id())) {
                            mentorDetails.add(mentorDetail); // Add mentor details to the list
                            uniqueMentorIds.add(mentorDetail.get_id()); // Add mentor ID to the HashSet
                        }
                    }
                }
            }
        }
        List<UserIdAndNameDto> searchFilterMentorDetails = new ArrayList<>();
        if (!StringUtils.isEmpty(searchString)) {
            for (UserIdAndNameDto mentorData : mentorDetails) {
                String mentorNameLowerCase = mentorData.getName().toLowerCase();
                String searchStringLowerCase = searchString.toLowerCase();
                if (mentorNameLowerCase.contains(searchStringLowerCase)) {
                    searchFilterMentorDetails.add(mentorData);
                }
            }
        } else {
            searchFilterMentorDetails = mentorDetails;
        }

        Collections.sort(searchFilterMentorDetails, new Comparator<UserIdAndNameDto>() {
            @Override
            public int compare(UserIdAndNameDto mentor1, UserIdAndNameDto mentor2) {
                if (sortKey.equals("name")) {
                    return sortDirection == 1 ? mentor1.getName().compareTo(mentor2.getName()) : mentor2.getName().compareTo(mentor1.getName());
                }
                return 0;
            }
        });

        // Apply pagination
        List<UserIdAndNameDto> paginatedMentors = Pagenation.paginateWithoutPageIndexConversion(searchFilterMentorDetails, pageNumber, pageSize);
        return new ApiResponseWithCount(searchFilterMentorDetails.size(), HttpStatus.OK.value(), "Mentor Fetched Successfully", paginatedMentors);
    }
    public ApiResponseWithCount getMentorOfTrainee(Integer pageNumber, Integer pageSize, Integer sortDirection, String sortKey, String searchString, String userId) {
        List<UserIdAndNameDto> mentorDetails = getMentorOfTrainee(userId);
        List<UserIdAndNameDto> searchFilterMentorDetails = new ArrayList<>();
        if (!StringUtils.isEmpty(searchString)) {
            for (UserIdAndNameDto mentorData : mentorDetails) {
                String mentorNameLowerCase = mentorData.getName().toLowerCase();
                String searchStringLowerCase = searchString.toLowerCase();
                if (mentorNameLowerCase.contains(searchStringLowerCase)) {
                    searchFilterMentorDetails.add(mentorData);
                }
            }
        }else {
            searchFilterMentorDetails = mentorDetails;
        }
        Collections.sort(searchFilterMentorDetails, new Comparator<UserIdAndNameDto>() {
            @Override
            public int compare(UserIdAndNameDto mentor1, UserIdAndNameDto mentor2) {
                if (sortKey.equals("name")) {
                    return sortDirection == 1 ? mentor1.getName().compareTo(mentor2.getName()) : mentor2.getName().compareTo(mentor1.getName());
                }
                return 0;
            }
        });

        // Apply pagination
        List<UserIdAndNameDto> paginatedMentors = Pagenation.paginateWithoutPageIndexConversion(searchFilterMentorDetails, pageNumber, pageSize);
        for (UserIdAndNameDto user : paginatedMentors) {
            Double rating = mentorFeedbackService.calculateOverallRatingForMentor(user.get_id());
            user.setOverallRating(rating);
        }
        return new ApiResponseWithCount(searchFilterMentorDetails.size(), HttpStatus.OK.value(), "Mentor Fetched Successfully", paginatedMentors);
    }
}
