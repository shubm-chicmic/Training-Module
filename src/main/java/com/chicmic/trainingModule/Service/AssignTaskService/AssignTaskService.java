package com.chicmic.trainingModule.Service.AssignTaskService;


import com.chicmic.trainingModule.Dto.AssignTaskDto.AssignTaskDto;
import com.chicmic.trainingModule.Entity.AssignedPlan;
import com.chicmic.trainingModule.Entity.Constants.TrainingStatus;
import com.chicmic.trainingModule.Entity.Plan;
import com.chicmic.trainingModule.Entity.PlanTask;
import com.chicmic.trainingModule.Repository.AssignTaskRepo;
import com.chicmic.trainingModule.Service.CourseServices.CourseService;
import com.chicmic.trainingModule.Service.PlanServices.PlanService;
import com.chicmic.trainingModule.Service.TestServices.TestService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;

@Service
@RequiredArgsConstructor
public class AssignTaskService {
    private final AssignTaskRepo assignTaskRepo;
    private final PlanService planService;
    private final CourseService courseService;
    private final TestService testService;
    private final MongoTemplate mongoTemplate;

    //TODO UPDATED AT TIME UPDATE AT UPDATE METHOD PENDING
    public AssignedPlan saveAssignTask(AssignedPlan assignedPlan){
        return assignTaskRepo.save(assignedPlan);
    }
    public AssignedPlan createAssignTask(AssignTaskDto assignTaskDto, String userId, Principal principal) {
        AssignedPlan assignTask = getAllAssignTasksByTraineeId(userId);
        if(assignTask != null) {
            List<Plan> plans = assignTask.getPlans();
            for (String planId : assignTaskDto.getPlanIds()) {
                Plan plan = planService.getPlanById(planId);
                if(!plans.contains(plan)){
                    plans.add(plan);
                }
            }
            assignTask.setPlans(plans);
            return assignTaskRepo.save(assignTask);
        }
        List<Plan> plans = planService.getPlanByIds(assignTaskDto.getPlanIds());

        assignTask = AssignedPlan.builder()
                .createdBy(principal.getName())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .plans(plans)
                .userId(userId)
                .trainingStatus(TrainingStatus.ONGOING)
                .date(assignTaskDto.getDate())
                .build();
        return assignTaskRepo.save(assignTask);
    }
    public List<AssignedPlan> getAllAssignTasks(String query, Integer sortDirection, String sortKey) {
        Query searchQuery = new Query()
                .addCriteria(Criteria.where("deleted").is(false));

        List<AssignedPlan> assignTasks = mongoTemplate.find(searchQuery, AssignedPlan.class);

        if (!sortKey.isEmpty()) {
            Comparator<AssignedPlan> assignTaskComparator = Comparator.comparing(assignTask -> {
                try {
                    Field field = AssignedPlan.class.getDeclaredField(sortKey);
                    field.setAccessible(true);
                    Object value = field.get(assignTask);
                    if (value instanceof String) {
                        return ((String) value).toLowerCase();
                    }
                    return value.toString();
                } catch (Exception e) {
                    e.printStackTrace();
                    return "";
                }
            });

            if (sortDirection == 1) {
                assignTasks.sort(assignTaskComparator.reversed());
            } else {
                assignTasks.sort(assignTaskComparator);
            }
        }

        return assignTasks;
    }
    public List<AssignedPlan> getAllAssignTasks(Integer pageNumber, Integer pageSize, String query, Integer sortDirection, String sortKey) {
        Pageable pageable;
        if (!sortKey.isEmpty()) {
            Sort.Direction direction = (sortDirection == 0) ? Sort.Direction.ASC : Sort.Direction.DESC;
            Sort sort = Sort.by(direction, sortKey);
            pageable = PageRequest.of(pageNumber, pageSize, sort);
        } else {
            pageable = PageRequest.of(pageNumber, pageSize);
        }

        Query searchQuery = new Query()
                .addCriteria(Criteria.where("taskName").regex(query, "i"))
                .addCriteria(Criteria.where("deleted").is(false))
                .with(pageable);

        List<AssignedPlan> assignTasks = mongoTemplate.find(searchQuery, AssignedPlan.class);

        if (!sortKey.isEmpty()) {
            Comparator<AssignedPlan> assignTaskComparator = Comparator.comparing(assignTask -> {
                try {
                    Field field = AssignedPlan.class.getDeclaredField(sortKey);
                    field.setAccessible(true);
                    Object value = field.get(assignTask);
                    if (value instanceof String) {
                        return ((String) value).toLowerCase();
                    }
                    return value.toString();
                } catch (Exception e) {
                    e.printStackTrace();
                    return "";
                }
            });

            if (sortDirection == 1) {
                assignTasks.sort(assignTaskComparator.reversed());
            } else {
                assignTasks.sort(assignTaskComparator);
            }
        }

        return assignTasks;
    }

    public AssignedPlan getAssignTaskById(String assignTaskId) {
        return assignTaskRepo.findById(assignTaskId).orElse(null);
    }

    public Boolean deleteAssignTaskById(String assignTaskId) {
        AssignedPlan assignTask = assignTaskRepo.findById(assignTaskId).orElse(null);
        if (assignTask != null) {
            assignTask.setDeleted(true);
            assignTaskRepo.save(assignTask);
            return true;
        } else {
            return false;
        }
    }

    public long countNonDeletedAssignTasks() {
        MatchOperation matchStage = match(Criteria.where("deleted").is(false));
        Aggregation aggregation = newAggregation(matchStage);
        AggregationResults<AssignedPlan> aggregationResults = mongoTemplate.aggregate(aggregation, "assignTask", AssignedPlan.class);
        return aggregationResults.getMappedResults().size();
    }

//    public AssignedPlan updateAssignTask(AssignTaskDto assignTaskDto, String assignTaskId) {
//        AssignedPlan assignTask = assignTaskRepo.findById(assignTaskId).orElse(null);
//        if (assignTask != null) {
//            List<Milestone> milestones = new ArrayList<>();
//            if (assignTaskDto.getMilestones() != null) {
//                for (List<Task> tasks : assignTaskDto.getMilestones()) {
//                    Milestone milestone = Milestone.builder()
//                            .tasks(tasks)
//                            .build();
//                    milestones.add(milestone);
//                }
//            }
//            // Only update properties from the DTO if they are not null
//            if (assignTaskDto.getTaskName() != null) {
//                assignTask.setTaskName(assignTaskDto.getTaskName());
//            }
//            if (assignTaskDto.getReviewers() != null) {
//                assignTask.setReviewers(assignTaskDto.getReviewers());
//            }
//            if (assignTaskDto.getTeams() != null) {
//                assignTask.setTeams(assignTaskDto.getTeams());
//            }
//            if (!milestones.isEmpty()) {
//                assignTask.setMilestones(milestones);
//            }
//            // Saving the updated assignTask
//            assignTaskRepo.save(assignTask);
//            return assignTask;
//        } else {
//            return null;
//        }
//        return null;
//    }

//    public AssignedPlan approve(AssignedPlan assignTask, String userId) {
//        Set<String> approvedBy = assignTask.getApprovedBy();
//        approvedBy.add(userId);
//        assignTask.setApprovedBy(approvedBy);
//        if (assignTask.re().size() == approvedBy.size()) {
//            assignTask.setApproved(true);
//        } else {
//            assignTask.setApproved(false);
//        }
//        return assignTaskRepo.save(assignTask);
//    }

//    public List<AssignTask> getAllAssignTasksByTraineeId(String traineeId) {
//        System.out.println(traineeId);
//            Query query = new Query(Criteria.where("userId").in(traineeId));
//            return mongoTemplate.find(query, AssignTask.class);
//    }
        public AssignedPlan getAllAssignTasksByTraineeId(String traineeId) {
            Query query = new Query(Criteria.where("userId").in(traineeId));
            return mongoTemplate.findOne(query, AssignedPlan.class);
        }

    public List<AssignedPlan> getAssignedPlansByPlan(Plan plan) {
        List<AssignedPlan> allAssignedPlans = assignTaskRepo.findAll();

        return allAssignedPlans.stream()
                .filter(assignedPlan -> assignedPlan != null &&
                        assignedPlan.getPlans() != null &&
                        assignedPlan.getPlans().stream()
                                .anyMatch(p -> p != null && p.get_id().equals(plan.get_id()) && !p.getDeleted()))
                .collect(Collectors.toList());
    }
    public AssignedPlan updateAssignTask(AssignedPlan assignedPlan) {
        if(assignedPlan.getPlans() == null)assignedPlan.setTrainingStatus(TrainingStatus.PENDING);
        else if (assignedPlan.getPlans() != null && assignedPlan.getPlans().size() == 0)assignedPlan.setTrainingStatus(TrainingStatus.PENDING);
        return assignTaskRepo.save(assignedPlan);
    }
}

