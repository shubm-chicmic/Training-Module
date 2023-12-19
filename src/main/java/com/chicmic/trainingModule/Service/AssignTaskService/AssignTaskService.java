package com.chicmic.trainingModule.Service.AssignTaskService;


import com.chicmic.trainingModule.Dto.AssignTaskDto.AssignTaskDto;
import com.chicmic.trainingModule.Entity.AssignTask.AssignTask;
import com.chicmic.trainingModule.Entity.Plan.Plan;
import com.chicmic.trainingModule.Repository.AssignTaskRepo;
import com.chicmic.trainingModule.Service.PlanServices.PlanService;
import com.chicmic.trainingModule.Util.CustomObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.security.Principal;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;

@Service
@RequiredArgsConstructor
public class AssignTaskService {
    private final AssignTaskRepo assignTaskRepo;
    private final PlanService planService;
    private final MongoTemplate mongoTemplate;
    public AssignTask createAssignTask(AssignTaskDto assignTaskDto, Principal principal) {

        List<Plan> plan = planService.getPlanByIds(assignTaskDto.getPlanIds());
        AssignTask assignTask = AssignTask.builder()
                .createdBy(principal.getName())
                .plans(plan)
                .users(assignTaskDto.getUsers())
                .reviewers(assignTaskDto.getReviewers())
                .build();
        return assignTaskRepo.save(assignTask);
    }
    public List<AssignTask> getAllAssignTasks(String query, Integer sortDirection, String sortKey) {
        Query searchQuery = new Query()
                .addCriteria(Criteria.where("deleted").is(false));

        List<AssignTask> assignTasks = mongoTemplate.find(searchQuery, AssignTask.class);

        if (!sortKey.isEmpty()) {
            Comparator<AssignTask> assignTaskComparator = Comparator.comparing(assignTask -> {
                try {
                    Field field = AssignTask.class.getDeclaredField(sortKey);
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
    public List<AssignTask> getAllAssignTasks(Integer pageNumber, Integer pageSize, String query, Integer sortDirection, String sortKey) {
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

        List<AssignTask> assignTasks = mongoTemplate.find(searchQuery, AssignTask.class);

        if (!sortKey.isEmpty()) {
            Comparator<AssignTask> assignTaskComparator = Comparator.comparing(assignTask -> {
                try {
                    Field field = AssignTask.class.getDeclaredField(sortKey);
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

    public AssignTask getAssignTaskById(String assignTaskId) {
        return assignTaskRepo.findById(assignTaskId).orElse(null);
    }

    public Boolean deleteAssignTaskById(String assignTaskId) {
        AssignTask assignTask = assignTaskRepo.findById(assignTaskId).orElse(null);
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
        Aggregation aggregation = Aggregation.newAggregation(matchStage);
        AggregationResults<AssignTask> aggregationResults = mongoTemplate.aggregate(aggregation, "assignTask", AssignTask.class);
        return aggregationResults.getMappedResults().size();
    }

    public AssignTask updateAssignTask(AssignTaskDto assignTaskDto, String assignTaskId) {
//        AssignTask assignTask = assignTaskRepo.findById(assignTaskId).orElse(null);
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
        return null;
    }

    public AssignTask approve(AssignTask assignTask, String userId) {
        Set<String> approvedBy = assignTask.getApprovedBy();
        approvedBy.add(userId);
        assignTask.setApprovedBy(approvedBy);
        if (assignTask.getReviewers().size() == approvedBy.size()) {
            assignTask.setApproved(true);
        } else {
            assignTask.setApproved(false);
        }
        return assignTaskRepo.save(assignTask);
    }

    public List<AssignTask> getAllAssignTasksByTraineeId(String traineeId) {
        System.out.println(traineeId);
            Query query = new Query(Criteria.where("users").in(traineeId));
            return mongoTemplate.find(query, AssignTask.class);

    }
}

