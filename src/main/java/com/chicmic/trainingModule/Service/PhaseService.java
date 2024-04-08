package com.chicmic.trainingModule.Service;

import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponse;
import com.chicmic.trainingModule.Entity.*;
import com.chicmic.trainingModule.Entity.Constants.EntityType;
import com.chicmic.trainingModule.Entity.Constants.PlanType;
import com.chicmic.trainingModule.Entity.Constants.ProgessConstants;
import com.chicmic.trainingModule.ExceptionHandling.ApiException;
import com.chicmic.trainingModule.Repository.*;
import com.chicmic.trainingModule.Service.PlanServices.PlanService;
import com.chicmic.trainingModule.Service.UserProgressService.UserProgressService;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.mongodb.client.result.DeleteResult;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.formula.functions.T;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;

import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.*;
import java.util.stream.Collectors;
//TODO we need to add validation to create a plan phase that phase should not be repeated
@Service
@RequiredArgsConstructor
public class PhaseService {
    private final MongoTemplate mongoTemplate;
    private final PhaseRepo phaseRepo;
    private final TaskRepo taskRepo;
    private final SubTaskRepo subTaskRepo;
    private final PlanTaskRepo planTaskRepo;
    private final PlanRepo planRepo;
    private final UserProgressService userProgressService;
//    @Transactional(rollbackFor = Exception.class)
    public List<Phase<Task>> createPhases(List<Phase<Task>> phases, Object entity, Integer entityType, Boolean isCourseIsAddingFromScript) {
        try {
            Boolean error = false;
            Set<String> errorPhasesName = new HashSet<>();
            System.out.println("Phaes = " + phases);
            Set<String> phaseIds = phases.stream()
                    .map(Phase::get_id)
                    .filter(id -> id != null) // Filter out null ids
                    .collect(Collectors.toSet());
            System.out.println(phaseIds);
            System.out.println(phaseIds.size());
            if (entity instanceof Course) {
                Course course = (Course) entity;
                System.out.println("*********************************************************************");
                System.out.println(course.getPhases());
                if (course.getPhases() != null && !course.getPhases().isEmpty() && phaseIds.size() > 0) {
                    System.out.println("Im in");
                    for (Phase<Task> phase : course.getPhases()) {
                        System.out.println("*********************************************************************");

                        if (!phaseIds.contains(phase.get_id())) {
                            System.out.println("*********************************************************************");

                            System.out.println("Deleted Phase " + phase.getName());
                            // Check if the entity type is Test
                            List<PlanTask> planTasks = planTaskRepo.findByMilestoneId(phase.get_id());
                            System.out.println("\u001B[43m planTasks " + planTasks.size());
                            if(planTasks.size() > 0){
                                error = true;
                                errorPhasesName.add(phase.getName());
//                                continue;
//                                throw new ApiException(HttpStatus.BAD_REQUEST, "Already Assigned " + name + " Cannot Be Deleted");
                            }
                            // Delete the phase
                        }
                    }
                    if(!error) {
                        for (Phase<Task> phase : course.getPhases()) {
                            if (!phaseIds.contains(phase.get_id())) {
                                // Delete the phase
                                deletePhase(phase);
                            }
                        }
                    }
                }
            }else if(entity instanceof Test) {
                System.out.println("***********************************************************");
                Test test = (Test) entity;
                if(test.getMilestones() != null && !test.getMilestones().isEmpty() && phaseIds.size() > 0) {
//                    Set<String> phaseIds = phases.stream().map(Phase::get_id).collect(Collectors.toSet());
                    System.out.println("Milestones : " + test.getMilestones());
                    for (Phase<Task> milestone: test.getMilestones()) {
                        System.out.println("Phase id " + phaseIds);
                        System.out.println("milesotne id " + milestone.get_id());
                        if(!phaseIds.contains(milestone.get_id())){
                            System.out.println("Deleted Milestone " + milestone.getName());
                            //delete milestone
                            List<PlanTask> planTasks = planTaskRepo.findByMilestoneId(milestone.get_id());
                            if(planTasks.size() > 0){
                                error = true;
                                errorPhasesName.add(milestone.getName());
                                continue;
//                                throw new ApiException(HttpStatus.BAD_REQUEST, "Already Assigned " + name + " Cannot Be Deleted");
                            }
                        }
                    }
                    if(!error) {
                        System.out.println("Deleted Milestone " + test.getMilestones().size());
                        System.out.println("Milestones " + test.getMilestones());
                        System.out.println("Milestone " + phaseIds);
                        for (Phase<Task> milestone: test.getMilestones()) {
                            if(!phaseIds.contains(milestone.get_id())){
                                // Delete the phase
//                                deletePhase(phase);
                                deletePhase(milestone);

                            }
                        }
                    }
                }
            }
            if(error){
                String resultantError = String.join(", ", errorPhasesName);
                System.out.println("\u001B[33m" + resultantError);
                throw new ApiException(HttpStatus.BAD_REQUEST, "Already Assigned " + resultantError + " Cannot Be Deleted!");
            }
            int count = 0;
            String phaseNameInitial = "";
            if (entityType == EntityType.TEST) {
                phaseNameInitial = "MILESTONE ";
            } else {
                phaseNameInitial = "PHASE ";
            }
            System.out.println("\u001B[35m " + phases);
            List<Phase<Task>> createdPhases = new ArrayList<>();
            for (Phase<Task> phase : phases) {
                Phase<Task> newPhase = new Phase<>();
                if (phase.get_id() == null || phase.get_id().isEmpty()) {
                    newPhase.set_id(String.valueOf(new ObjectId()));
                } else {
                    System.out.println("phase id " + phase.get_id());
                    newPhase = (Phase<Task>) getPhaseById(phase.get_id());
                }
                count++;
                System.out.println("\u001B[33m" + phase + "\u001B[0m");
                System.out.println("\u001B[34m" + newPhase + "\u001B[0m");

    //            System.out.println("\u001B[32m" + task + "\u001B[0m");
                List<Task> tasks = createTasks(phase.getTasks(), newPhase, entityType);
                if(isCourseIsAddingFromScript) {
                    newPhase.setName(phaseNameInitial + count);
                }else {
                    newPhase.setName(phase.getName());
                }
                newPhase.setEntityType(entityType);
                newPhase.setTasks(tasks);
                newPhase.setEntity(entity);
                createdPhases.add(phaseRepo.save(newPhase));
            }
            System.out.println("*********************************************************************");


            return createdPhases;
        } catch (Exception e) {
            e.printStackTrace();
            // Rollback the transaction
//            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw new ApiException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
    public List<Phase<PlanTask>> createPlanPhases(List<Phase<PlanTask>> phases, Object entity) {
        try {
            if (entity instanceof Plan) {
                Plan plan = (Plan) entity;
                if (plan.getPhases() != null && !plan.getPhases().isEmpty()) {
                    Set<String> phaseIds = phases.stream().map(Phase::get_id).collect(Collectors.toSet());
                    for (Phase<PlanTask> phase : plan.getPhases()) {
                        if(!phaseIds.contains(phase.get_id())){
                            System.out.println("Deleted Phase " + phase.getName());
                            //delete phase
                            deletePhase(phase);
                        }
                    }
                }
            }
            System.out.println("\u001B[35m " + phases);
            List<Phase<PlanTask>> createdPhases = new ArrayList<>();
            for (Phase<PlanTask> phase : phases) {
                Phase<PlanTask> newPhase = new Phase<>();
                if (phase.get_id() == null || phase.get_id().isEmpty() || phase.get_id().isBlank()) {
                    newPhase.set_id(String.valueOf(new ObjectId()));
                } else {
                    newPhase = (Phase<PlanTask>) getPhaseById(phase.get_id());
                }
                System.out.println("\u001B[33m" + phase + "\u001B[0m");
    //            System.out.println("\u001B[32m" + task + "\u001B[0m");
                List<PlanTask> tasks = createPlanTasks(phase.getTasks(), newPhase, (Plan) entity);

                newPhase.setName(phase.getName());
                newPhase.setEntityType(EntityType.PLAN);
                newPhase.setTasks(tasks);
                newPhase.setEntity(entity);
                createdPhases.add(phaseRepo.save(newPhase));
            }

            return createdPhases;
        } catch (Exception e) {
            e.printStackTrace();
            // Rollback the transaction
            throw new ApiException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
    private List<PlanTask> createPlanTasks(List<PlanTask> planTasks, Phase<PlanTask> phase, Plan plan) {
        try {

            List<PlanTask> createdPlanTasks = new ArrayList<>();
            for (PlanTask planTask : planTasks) {
                PlanTask newPlanTask = new PlanTask();
                System.out.println("plan task id " + planTask.get_id());
                if (planTask.get_id() == null || planTask.get_id().isEmpty() || planTask.get_id().isBlank()) {
                    System.out.println("sdisdois " + planTask.get_id());

                    newPlanTask.set_id(String.valueOf(new ObjectId()));
                    System.out.println("new Id = " + newPlanTask.get_id());
                } else {
                    System.out.println("fljgdkofdklf " + planTask.get_id());


                    newPlanTask = planTaskRepo.findById(planTask.get_id()).orElse(null);
                    List<Object> milestones = newPlanTask.getMilestones(); // original milestones
                    if (milestones != null) {
                        for (Object milestone : milestones) {
                            if (!planTask.getMilestones().contains(milestone)) {
                                // this milestone is deleted
                                // delete all userProgress of this milestone
                                System.out.println("\u001B[42m PlanTask not countain " + planTask.get_id() + "\u001B[0m");
                                userProgressService.deleteAllBySubTaskId(plan.get_id(), (String) milestone);
                            }
                        }
                    }
                    if (newPlanTask.getPlanType() == PlanType.VIVA) {
                        List<Object> milestones1 = newPlanTask.getMilestones(); // original milestones
                        for (Object milestone : milestones1) {
                            if (!planTask.getMilestones().contains(milestone)) {
                                // this milestone is deleted
                                // delete all userProgress of this milestone
                                System.out.println("\u001B[42m PlanTask not countain " + planTask.get_id() + "\u001B[0m");
                                userProgressService.deleteByMilestoneId(plan.get_id(), newPlanTask.get_id());
                            }
                        }
                    }
                    System.out.println("new Id 2 = " + newPlanTask.get_id());

                }
                Integer totalTask = 0;
                Integer totalEstimatedtime = 0;
                if (planTask.getMilestones() != null) {
                    for (Object milestone : planTask.getMilestones()) {
                        Phase<Task> coursePhase = (Phase<Task>) getPhaseById((String) milestone);
                        totalTask += coursePhase.getTotalTasks();
                        totalEstimatedtime += coursePhase.getEstimatedTimeInSeconds();
                    }
                }
                newPlanTask.setPlans(plan);
                newPlanTask.setTotalTasks(totalTask);
                newPlanTask.setMentor(planTask.getMentorIds());
                newPlanTask.setMilestones(planTask.getMilestones());
                newPlanTask.setPlanType(planTask.getPlanType());
                newPlanTask.setPlan(planTask.getPlan());
                newPlanTask.setDate(planTask.getDate());
                newPlanTask.setEstimatedTime(planTask.getEstimatedTime());
                newPlanTask.setPhase(phase);
                if (planTask.getPlanType() == PlanType.COURSE || planTask.getPlanType() == PlanType.TEST) {
                    newPlanTask.setEstimatedTimeInSeconds(totalEstimatedtime);
                }
                createdPlanTasks.add(planTaskRepo.save(newPlanTask));
    //            String id = (task.get_id() == null || task.get_id().isEmpty()) ? String.valueOf(new ObjectId()) : task.get_id();
    //            task.set_id(id);
            }
            if(phase != null){
                List<PlanTask> originalPlanTasks = phase.getTasks();
                Set<String> planTaskIds = planTasks.stream().map(PlanTask::get_id).collect(Collectors.toSet());

                for (PlanTask originalPlanTask : originalPlanTasks) {
                    if (!planTaskIds.contains(originalPlanTask.get_id())) {
                        System.out.println("\u001B[31m Deleted planTask " + originalPlanTask.getPlan());
                        //delete planTask
                        deleteTask(originalPlanTask);
                    }
                }
            }
            return createdPlanTasks;
        } catch (Exception e) {
            e.printStackTrace();
            // Rollback the transaction
            throw new ApiException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
    public List<Task> createTasks(List<Task> tasks, Phase<Task> phase, Integer entityType) {
        try {

            List<Task> createdTasks = new ArrayList<>();
            for (Task task : tasks) {
                Task newTask = new Task();
                if (task.get_id() == null || task.get_id().isEmpty() || task.get_id().isBlank()) {
                    newTask.set_id(String.valueOf(new ObjectId()));
                } else {
                    newTask = taskRepo.findById(task.get_id()).orElse(null);
                }
    //            String id = (task.get_id() == null || task.get_id().isEmpty()) ? String.valueOf(new ObjectId()) : task.get_id();
    //            task.set_id(id);
                List<SubTask> subTasks = createSubTasks(task.getSubtasks(), newTask, entityType, phase);
                newTask.setEntityType(entityType);
                newTask.setMainTask(task.getMainTask());
                newTask.setSubtasks(subTasks);
                newTask.setPhase(phase);
                System.out.println("\u001B[31m" + newTask + "\u001B[0m");
                System.out.println("\u001B[32m" + task + "\u001B[0m");

                createdTasks.add(taskRepo.save(newTask));
            }
            if(phase != null){
                List<Task> originalTasks = phase.getTasks();
                Set<String> taskIds = tasks.stream().map(Task::get_id).collect(Collectors.toSet());

                for (Task originalTask : originalTasks) {
                    if (!taskIds.contains(originalTask.get_id())) {
                        System.out.println("\u001B[31m Deleted Task " + originalTask.getMainTask());
                        //delete task
                        deleteTask(originalTask);
                    }
                }
            }
            return createdTasks;
        } catch (Exception e) {
            e.printStackTrace();
            // Rollback the transaction
            throw new ApiException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    public List<SubTask> createSubTasks(List<SubTask> subTasks, Task task, Integer entityType, Phase<Task> phase) {
        try {

            List<SubTask> createdSubTasks = new ArrayList<>();
            for (SubTask subTask : subTasks) {
                SubTask newSubTask = new SubTask();
                if (subTask.get_id() == null || subTask.get_id().isEmpty() || subTask.get_id().isBlank()) {
                    newSubTask.set_id(String.valueOf(new ObjectId()));
                    // new subtask is created
                    // task count to plantask
                    List<PlanTask> planTasks = planTaskRepo.findByMilestoneId(phase.get_id());
                    for (PlanTask planTask : planTasks) {
                        planTask.setTotalTasks(planTask.getTotalTasks() + 1);
                        Integer estimatedTime = planTask.getEstimatedTimeInSeconds();
                        planTask.setEstimatedTimeInSeconds(estimatedTime + subTask.getEstimatedTimeInSeconds());

                        Phase<PlanTask> planTaskPhase = planTask.getPhase();
                        Plan plan = (Plan)planTaskPhase.getEntity();
                        planTaskPhase.setEstimatedTimeInSeconds(planTaskPhase.getEstimatedTimeInSeconds() + subTask.getEstimatedTimeInSeconds());
                        plan.setEstimatedTimeInSeconds(plan.getEstimatedTimeInSeconds() + subTask.getEstimatedTimeInSeconds());
                        planTaskRepo.save(planTask);
                        phaseRepo.save(planTaskPhase);
                        planRepo.save(plan);

                    }
                } else {
                    newSubTask = subTaskRepo.findById(subTask.get_id()).orElse(null);
                    List<PlanTask> planTasks = planTaskRepo.findByMilestoneId(phase.get_id());
                    for (PlanTask planTask : planTasks) {
    //                    planTask.setTotalTasks(planTask.getTotalTasks() + 1);
                        Integer estimatedTime = planTask.getEstimatedTimeInSeconds();
                        Phase<PlanTask> planTaskPhase = planTask.getPhase();
                        Plan plan = (Plan)planTaskPhase.getEntity();


                        if(newSubTask.getEstimatedTimeInSeconds() > subTask.getEstimatedTimeInSeconds()){
                            Integer changeInEstimateTime = (newSubTask.getEstimatedTimeInSeconds() - subTask.getEstimatedTimeInSeconds());
                            estimatedTime = estimatedTime - changeInEstimateTime;
                            planTaskPhase.setEstimatedTimeInSeconds(planTaskPhase.getEstimatedTimeInSeconds() - changeInEstimateTime);
                            plan.setEstimatedTimeInSeconds(plan.getEstimatedTimeInSeconds() - changeInEstimateTime);

                        }else if(newSubTask.getEstimatedTimeInSeconds() < subTask.getEstimatedTimeInSeconds()){
                            Integer changeInEstimateTime = (subTask.getEstimatedTimeInSeconds() - newSubTask.getEstimatedTimeInSeconds());

                            estimatedTime = estimatedTime + changeInEstimateTime;
                            planTaskPhase.setEstimatedTimeInSeconds(planTaskPhase.getEstimatedTimeInSeconds() + changeInEstimateTime);
                            plan.setEstimatedTimeInSeconds(plan.getEstimatedTimeInSeconds() + changeInEstimateTime);

                        }
                        if (estimatedTime < 0) estimatedTime = 0;
                        planTask.setEstimatedTimeInSeconds(estimatedTime);
                        planTaskRepo.save(planTask);
                        phaseRepo.save(planTaskPhase);
                        if(plan != null)
                        planRepo.save(plan);
                    }
                }
    //            String id = (subTask.get_id() == null || subTask.get_id().isEmpty()) ? String.valueOf(new ObjectId()) : subTask.get_id();
    //            subTask.set_id(id);
                newSubTask.setSubTask(subTask.getSubTask());
                newSubTask.setLink(subTask.getLink());
                newSubTask.setReference(subTask.getReference());
                newSubTask.setEstimatedTime(subTask.getEstimatedTime());
                newSubTask.setEntityType(entityType);
                newSubTask.setTask(task);
                newSubTask.setPhase(subTask.getPhase());
                createdSubTasks.add(subTaskRepo.save(newSubTask));
            }
            if(task != null){
                List<SubTask>  originalSubTasks = task.getSubtasks();
                Set<String> subTaskIds = subTasks.stream().map(SubTask::get_id).collect(Collectors.toSet());

                for (SubTask originalSubTask : originalSubTasks) {
                    if (!subTaskIds.contains(originalSubTask.get_id())) {
                        System.out.println("\u001B[31m Deleted subtask " + originalSubTask.getSubTask());
                        //delete subtask
                        deleteSubtask(originalSubTask);
                    }
                }
            }

            return createdSubTasks;
        } catch (Exception e) {
            e.printStackTrace();
            // Rollback the transaction
            throw new ApiException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    public Phase<?> getPhaseById(String phaseId) {
        Phase<?> phase = phaseRepo.findById(phaseId).orElse(null);
        return phase != null && !phase.getIsDeleted() ? phase : null;
    }


    public List<Phase> getPhaseByIds(List<String> phaseIds) {
        List<Phase> phases = phaseRepo.findAllById(phaseIds);
        Map<String, Phase> phaseMap = phases.stream()
                .collect(Collectors.toMap(Phase::get_id, phase -> phase));

        // Create a list to hold the phases in the order of phaseIds
        List<Phase> sortedPhases = phaseIds.stream()
                .map(phaseMap::get)
                .collect(Collectors.toList());

        return sortedPhases;
    }

    public Task getTaskById(String taskId) {
        Task task = taskRepo.findById(taskId).orElse(null);
        return task.getIsDeleted() ? null : task;
    }

    public PlanTask getPlanTaskById(String taskId) {
        PlanTask task = planTaskRepo.findById(taskId).orElse(null);
        return task.getIsDeleted() ? null : task;
    }

    public SubTask getSubTaskById(String taskId) {
        SubTask task = subTaskRepo.findById(taskId).orElse(null);
        if(task == null)return null;
        return task.getIsDeleted() ? null : task;
    }

    public <T> boolean deletePhase(Phase<T> phase) {
        if (phase != null) {
            List<PlanTask> planTasks = planTaskRepo.findByMilestoneId(phase.get_id());
            if (planTasks.size() > 0) {
                return false;
            }

            // Deleting tasks and subtasks associated with the phase
            List<?> tasks = phase.getTasks();
            if (tasks != null) {
                for (Object task : tasks) {
                    if (task instanceof Task) {
                        deleteTask((Task) task);
                    } else if (task instanceof PlanTask) {
                        deleteTask((PlanTask) task);
                    }
                }
            }
            if (phase.getEntityType() == EntityType.TEST) {
                Test test = (Test) phase.getEntity();
            } else if (phase.getEntityType() == EntityType.COURSE) {
                Course course = (Course) phase.getEntity();
            }

            phase.setIsDeleted(true);
//            planService.findIfPhaseExists(phase);


            phaseRepo.save(phase);
            return true;
        }
        return false;
    }

    public boolean deleteTask(Task task) {
        if (task != null) {
            List<SubTask> subtasks = task.getSubtasks();
            if (subtasks != null) {
                for (SubTask subTask : subtasks) {
                    deleteSubtask(subTask);
//                    subTaskRepo.save(subTask);
                }
            }
            task.setIsDeleted(true);
            taskRepo.save(task);
            return true;
        }
        return false;
    }

    public boolean deleteTask(PlanTask task) {
        if (task != null) {
            Phase<PlanTask> phase = task.getPhase();
            Plan plan = (Plan)phase.getEntity();
            if(task.getPlanType() == PlanType.VIVA || task.getPlanType() == PlanType.PPT){
                userProgressService.deleteByMilestoneId(plan.get_id(), task.get_id());
            }else {
                List<Object> milestones = task.getMilestones();
                if (milestones != null) {
                    for (Object milestone : milestones) {
                        // delete all userProgress of this milestone
                        userProgressService.deleteAllBySubTaskId(plan.get_id(), (String) milestone);
                    }
                }
            }
            task.setIsDeleted(true);
            planTaskRepo.save(task);
            return true;
        }
        return false;
    }

    public boolean deleteSubtask(SubTask subTask) {
        if (subTask != null) {

            deleteUserProgressBySubTaskId(subTask.get_id());
            List<Task> taskList = findTasksBySubtask(subTask);
            System.out.println("\u001B[41m PhaseId" + taskList.size());

            Task task = subTask.getTask();
            Phase<Task> phase = task.getPhase();
            System.out.println("\u001B[41m PhaseId" + phase.get_id());
            Integer phaseEstimatedTime = phase.getEstimatedTimeInSeconds();
            Integer phaseTotalTasks = phase.getTotalTasks();
            phase.setTotalTasks(phaseTotalTasks - 1);
            phase.setEstimatedTimeInSeconds(phaseEstimatedTime == 0 ? 0 : phaseEstimatedTime - subTask.getEstimatedTimeInSeconds());
            List<PlanTask> planTasks = planTaskRepo.findByMilestoneId(phase.get_id());
            for (PlanTask planTask : planTasks) {
                planTask.setTotalTasks(planTask.getTotalTasks() == 0 ? 0 : planTask.getTotalTasks() - 1);
                Integer estimatedTime = planTask.getEstimatedTimeInSeconds();
                planTask.setEstimatedTimeInSeconds(estimatedTime == 0 ? 0 : estimatedTime - subTask.getEstimatedTimeInSeconds());

                Phase<PlanTask> planTaskPhase = planTask.getPhase();
                Plan plan = (Plan)planTaskPhase.getEntity();
                planTaskPhase.setEstimatedTimeInSeconds(planTaskPhase.getEstimatedTimeInSeconds() - subTask.getEstimatedTimeInSeconds());
                plan.setEstimatedTimeInSeconds(plan.getEstimatedTimeInSeconds() - subTask.getEstimatedTimeInSeconds());
                planTaskRepo.save(planTask);
                phaseRepo.save(planTaskPhase);
                planRepo.save(plan);
            }
            Integer taskEstimatedTime = task.getEstimatedTimeInSeconds();
            task.setEstimatedTimeInSeconds(taskEstimatedTime == 0 ? 0 : taskEstimatedTime - subTask.getEstimatedTimeInSeconds());
            taskRepo.save(task);
            phaseRepo.save(phase);


            subTask.setIsDeleted(true);
            subTaskRepo.save(subTask);
            return true;
        }
        return false;
    }

    public long deleteUserProgressBySubTaskId(String subTaskId) {
        Query query = new Query(Criteria.where("subTaskId").is(subTaskId));
        DeleteResult result = mongoTemplate.remove(query, UserProgress.class);
        System.out.println("UsrProgress Deleted " + result.getDeletedCount());
        return result.getDeletedCount();
    }

    public List<Task> findTasksBySubtask(SubTask subTask) {
        return taskRepo.findBySubtasks(subTask);
    }

    public Integer countTotalSubtask(List<Object> milestones) {
        Integer totalSubtask = 0;
        for (Object milestone : milestones) {
            Phase<Task> phase = (Phase<Task>) getPhaseById((String) milestone);
            for (Task task : phase.getTasks()) {
                for (SubTask subTask : task.getSubtasks()) {
                    totalSubtask += 1;
                }
            }
//            totalSubtask += phase.getTotalTasks();
        }
        return totalSubtask;
    }
}
