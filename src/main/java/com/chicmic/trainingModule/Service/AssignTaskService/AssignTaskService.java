package com.chicmic.trainingModule.Service.AssignTaskService;


import com.chicmic.trainingModule.Dto.AssignTaskDto.AssignTaskDto;
import com.chicmic.trainingModule.Dto.AssignTaskDto.TaskCompleteDto;
import com.chicmic.trainingModule.Entity.AssignTask.AssignTask;
import com.chicmic.trainingModule.Entity.AssignTask.AssignTaskPlanTrack;
import com.chicmic.trainingModule.Entity.Course.Course;
import com.chicmic.trainingModule.Entity.Course.CourseSubTask;
import com.chicmic.trainingModule.Entity.Course.CourseTask;
import com.chicmic.trainingModule.Entity.Plan.Phase;
import com.chicmic.trainingModule.Entity.Plan.Plan;
import com.chicmic.trainingModule.Entity.Plan.Task;
import com.chicmic.trainingModule.Entity.Test.Milestone;
import com.chicmic.trainingModule.Entity.Test.Test;
import com.chicmic.trainingModule.Entity.Test.TestSubTask;
import com.chicmic.trainingModule.Entity.Test.TestTask;
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
    public AssignTask createAssignTask(AssignTaskDto assignTaskDto, String userId, Principal principal) {
        AssignTask assignTask = getAllAssignTasksByTraineeId(userId);
        if(assignTask != null) {
            return null;
        }
        List<Plan> plans = planService.getPlanByIds(assignTaskDto.getPlanIds());
        for (Plan plan : plans) {
            for (Phase phase : plan.getPhases()){
                for (Task task : phase.getTasks()) {
                    String planId = (String)task.getPlan();
                    AssignTaskPlanTrack modifiedPlan = AssignTaskPlanTrack.builder()
                            .isCompleted(false)
                            ._id(planId)
                            .build();
                    System.out.println(task.getMilestones());
                    Object milestonesObject = task.getMilestones();
                    List<String> milestoneList;
                    if (milestonesObject instanceof String) {
                        milestoneList = new ArrayList<>();
                        milestoneList.add((String) milestonesObject);
                    } else if (milestonesObject instanceof List) {
                        milestoneList = (List<String>) milestonesObject;
                    } else {
                        milestoneList = Collections.emptyList();
                    }
                    List<String> milestonesId = milestoneList;
                    List<AssignTaskPlanTrack> milestones = new ArrayList<>();
                    for (String milestone : milestonesId){
                        AssignTaskPlanTrack assignTaskPlanTrack = null;
                        if(task.getPlanType() == 1) {
                            Course course = courseService.getCourseById(planId);
                            for (com.chicmic.trainingModule.Entity.Course.Phase coursePhase : course.getPhases()) {
                                if (coursePhase.get_id().equals(milestone)){
                                    List<AssignTaskPlanTrack> assignTaskCourse = new ArrayList<>();
                                    for (CourseTask courseTask : coursePhase.getTasks()) {
                                        List<AssignTaskPlanTrack> assignTaskCourseSubTask = new ArrayList<>();
                                        for (CourseSubTask courseSubTask : courseTask.getSubtasks()) {
                                            AssignTaskPlanTrack subTasktemp = AssignTaskPlanTrack.builder()
                                                    ._id(courseSubTask.get_id())
                                                    .isCompleted(false)
                                                    .build();
                                            assignTaskCourseSubTask.add(subTasktemp);
                                        }
                                        AssignTaskPlanTrack taskTrackTemp = AssignTaskPlanTrack.builder()
                                                ._id(courseTask.get_id())
                                                .isCompleted(false)
                                                .subtasks(assignTaskCourseSubTask)
                                                .build();
                                        assignTaskCourse.add(taskTrackTemp);
                                    }
                                    assignTaskPlanTrack = AssignTaskPlanTrack.builder()
                                            ._id(milestone)
                                            .isCompleted(false)
                                            .tasks(assignTaskCourse)
                                            .build();
                                }
                            }

                        }else if(task.getPlanType() == 2){
                            Test test = testService.getTestById(planId);
                            for (Milestone milestone1 : test.getMilestones()) {
                                if (milestone1.get_id().equals(milestone)){
                                    List<AssignTaskPlanTrack> assignTaskTest = new ArrayList<>();
                                    for (TestTask testTask : milestone1.getTasks()) {
                                        List<AssignTaskPlanTrack> assignTaskTestSubTask = new ArrayList<>();
                                        for (TestSubTask testSubTask : testTask.getSubtasks()) {
                                            AssignTaskPlanTrack subTasktemp = AssignTaskPlanTrack.builder()
                                                    ._id(testSubTask.get_id())
                                                    .isCompleted(false)
                                                    .build();
                                            assignTaskTestSubTask.add(subTasktemp);
                                        }
                                        AssignTaskPlanTrack taskTrackTemp = AssignTaskPlanTrack.builder()
                                                ._id(testTask.get_id())
                                                .isCompleted(false)
                                                .subtasks(assignTaskTestSubTask)
                                                .build();
                                        assignTaskTest.add(taskTrackTemp);
                                    }
                                    assignTaskPlanTrack = AssignTaskPlanTrack.builder()
                                            ._id(milestone)
                                            .isCompleted(false)
                                            .tasks(assignTaskTest)
                                            .build();
                                }
                            }
                        }
                        milestones.add(assignTaskPlanTrack);
                    }
                    task.setPlan(modifiedPlan);
                    task.setMilestones(milestones);
                }
            }
        }

        assignTask = AssignTask.builder()
                .createdBy(principal.getName())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .plans(plans)
                .userId(userId)
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
        Aggregation aggregation = newAggregation(matchStage);
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

//    public List<AssignTask> getAllAssignTasksByTraineeId(String traineeId) {
//        System.out.println(traineeId);
//            Query query = new Query(Criteria.where("userId").in(traineeId));
//            return mongoTemplate.find(query, AssignTask.class);
//    }
        public AssignTask getAllAssignTasksByTraineeId(String traineeId) {
            Query query = new Query(Criteria.where("userId").in(traineeId));
            return mongoTemplate.findOne(query, AssignTask.class);
        }
    public AssignTask completeTask(TaskCompleteDto taskCompleteDto, Principal principal) {
        System.out.println("Task complete Dto " + taskCompleteDto);
        if (taskCompleteDto.getMilestone() != null) {
            System.out.println("Im completing this task ");
            AssignTask assignTask = assignTaskRepo.findById(taskCompleteDto.getAssignTaskId()).orElse(null);
            return markMilestoneAsCompleted(taskCompleteDto);
        }else {
            System.out.println("Im completing this task ");
            AssignTask assignTask = assignTaskRepo.findById(taskCompleteDto.getAssignTaskId()).orElse(null);
            return markMilestoneAsCompleted(taskCompleteDto);
        }
//        return null;
    }

    public AssignTask markMilestoneAsCompleted(TaskCompleteDto taskCompleteDto) {
        String assignTaskId = taskCompleteDto.getAssignTaskId();
        String planId = taskCompleteDto.getPlanId();
        String milestoneId = taskCompleteDto.getMilestone();
        String mainTaskId = taskCompleteDto.getMainTaskId();
        String subTaskId = taskCompleteDto.getSubtaskId();
        Query query = new Query(Criteria.where("_id").is(assignTaskId).and("plans._id").is(planId));
        List<Plan> plans = mongoTemplate.find(query, AssignTask.class)
                .stream()
                .flatMap(assignTask -> assignTask.getPlans().stream()
                        .filter(plan -> plan.get_id().equals(planId)))
                .collect(Collectors.toList());
        if(subTaskId == null && milestoneId != null) {
            System.out.println("\u001B[31m milestone id 1 ");

            one:
            for (Plan plan : plans) {
                if (plan.get_id().equals(planId)) {
                    for (Phase phase : plan.getPhases()) {
                        for (Task task : phase.getTasks()) {
                            List<AssignTaskPlanTrack> milestones = (List<AssignTaskPlanTrack>) task.getMilestones();
                            Integer countMilestoneComplete = 0;
                            for (AssignTaskPlanTrack milestone : milestones) {
                                if (milestone.get_id().equals(milestoneId)) {
                                    milestone.setIsCompleted(true);
                                    for(AssignTaskPlanTrack milestoneMainTask : milestone.getTasks()){
                                        milestoneMainTask.setIsCompleted(true);
                                        for (AssignTaskPlanTrack milestoneSubTask : milestoneMainTask.getSubtasks()) {
                                            milestoneSubTask.setIsCompleted(true);
                                        }
                                    }
//                                    break one;
                                }
                                if(milestone.getIsCompleted()) {
                                    countMilestoneComplete++;
                                }
                            }
                            if(countMilestoneComplete == milestones.size()){
                                ((AssignTaskPlanTrack)task.getPlan()).setIsCompleted(true);
                                break one;
                            }
                        }
                    }
                }
            }
        }else if (milestoneId == null) {
            System.out.println("\u001B[31m milestone id 2");
            one:
            for (Plan plan : plans) {
                if (plan.get_id().equals(planId)) {
                    for (Phase phase : plan.getPhases()) {
                        for (Task task : phase.getTasks()) {
                            System.out.println("Plan type = " + task.getPlanType());
                            if (task.getPlanType() == 4) {
                                phase.setIsCompleted(true);
                                break one;
                            }
                        }
                    }
                }
            }
        }
        else {
            System.out.println("\u001B[31m milestone id 3");

            one:
            for (Plan plan : plans) {
                if (plan.get_id().equals(planId)) {
                    for (Phase phase : plan.getPhases()) {
                        for (Task task : phase.getTasks()) {
                            List<AssignTaskPlanTrack> milestones = (List<AssignTaskPlanTrack>) task.getMilestones();
                            Integer countMilestoneComplete = 0;
                            for (AssignTaskPlanTrack milestone : milestones) {
                                if (milestone.get_id().equals(milestoneId)) {
                                    Integer countMainTaskComplete = 0;
                                    for(AssignTaskPlanTrack milestoneMainTask : milestone.getTasks()){
                                        if(milestoneMainTask.get_id().equals(mainTaskId)) {
                                            Integer countSubTaskComplete = 0;
                                            for (AssignTaskPlanTrack milestoneSubTask : milestoneMainTask.getSubtasks()) {
                                                if(milestoneSubTask.get_id().equals(subTaskId)){
                                                    milestoneSubTask.setIsCompleted(true);
//                                                    milestoneMainTask.setIsCompleted(true);
//                                                    milestone.setIsCompleted(true);
                                                }
                                                if(milestoneSubTask.getIsCompleted()){
                                                    countSubTaskComplete++;
                                                }
                                            }
                                            if(countSubTaskComplete == milestoneMainTask.getSubtasks().size()){
                                                milestoneMainTask.setIsCompleted(true);
                                            }

                                        }
                                        if(milestoneMainTask.getIsCompleted()) {
                                            countMainTaskComplete++;
                                        }
                                    }
                                    if(countMainTaskComplete == milestone.getTasks().size()){
                                        milestone.setIsCompleted(true);
//                                        break one;
                                    }

                                }
                                if(milestone.getIsCompleted()) {
                                    countMilestoneComplete++;
                                }
                            }
                            if(countMilestoneComplete == milestones.size()){
                                ((AssignTaskPlanTrack)task.getPlan()).setIsCompleted(true);
                                break one;
                            }
                        }
                    }
                }
            }
        }
        AssignTask assignTask = assignTaskRepo.findById(assignTaskId).orElse(null);
        assignTask.setPlans(plans);
        return assignTaskRepo.save(assignTask);
//        return null;
    }


}

