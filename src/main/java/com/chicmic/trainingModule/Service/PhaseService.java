package com.chicmic.trainingModule.Service;

import com.chicmic.trainingModule.Entity.*;
import com.chicmic.trainingModule.Entity.Constants.EntityType;
import com.chicmic.trainingModule.Entity.Constants.PlanType;
import com.chicmic.trainingModule.Entity.Constants.ProgessConstants;
import com.chicmic.trainingModule.Repository.*;
import com.chicmic.trainingModule.Service.PlanServices.PlanService;
import com.chicmic.trainingModule.Service.UserProgressService.UserProgressService;
import com.mongodb.client.result.DeleteResult;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.formula.functions.T;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;

import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PhaseService {
    private final MongoTemplate mongoTemplate;
    private final PhaseRepo phaseRepo;
    private final TaskRepo taskRepo;
    private final SubTaskRepo subTaskRepo;
    private final PlanTaskRepo planTaskRepo;
    private final UserProgressService userProgressService;
    public List<Phase<Task>> createPhases(List<Phase<Task>> phases, Object entity, Integer entityType) {
        int count = 0;
        String phaseNameInitial = "";
        if(entityType == EntityType.TEST){
            phaseNameInitial = "Milestone ";
        }else {
            phaseNameInitial = "Phase ";
        }
        System.out.println("\u001B[35m "+phases);
        List<Phase<Task>> createdPhases = new ArrayList<>();
        for (Phase<Task> phase : phases) {
            Phase<Task> newPhase = new Phase<>();
            if(phase.get_id() == null || phase.get_id().isEmpty()){
                newPhase.set_id(String.valueOf(new ObjectId()));
            }else {
                newPhase = (Phase<Task>) getPhaseById(phase.get_id());
            }
            count++;
            System.out.println("\u001B[33m" + phase + "\u001B[0m");
//            System.out.println("\u001B[32m" + task + "\u001B[0m");
            List<Task> tasks = createTasks(phase.getTasks(), newPhase, entityType);

            newPhase.setName(phaseNameInitial + count);
            newPhase.setEntityType(entityType);
            newPhase.setTasks(tasks);
            newPhase.setEntity(entity);
            createdPhases.add(phaseRepo.save(newPhase));
        }
        return createdPhases;
    }
    public List<Phase<PlanTask>> createPlanPhases(List<Phase<PlanTask>> phases, Object entity) {
        System.out.println("\u001B[35m "+phases);
        List<Phase<PlanTask>> createdPhases = new ArrayList<>();
        for (Phase<PlanTask> phase : phases) {
            Phase<PlanTask> newPhase = new Phase<>();
            if(phase.get_id() == null || phase.get_id().isEmpty() || phase.get_id().isBlank()){
                newPhase.set_id(String.valueOf(new ObjectId()));
            }else {
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
    }

    private List<PlanTask> createPlanTasks(List<PlanTask> planTasks, Phase<PlanTask> phase, Plan plan) {
        List<PlanTask> createdPlanTasks = new ArrayList<>();
        for (PlanTask planTask : planTasks) {
            PlanTask newPlanTask = new PlanTask();
            System.out.println("plan task id " + planTask.get_id());
            if(planTask.get_id() == null || planTask.get_id().isEmpty() || planTask.get_id().isBlank()){
                System.out.println("sdisdois " + planTask.get_id());

                newPlanTask.set_id(String.valueOf(new ObjectId()));
                System.out.println("new Id = " + newPlanTask.get_id());
            }else {
                System.out.println("fljgdkofdklf " + planTask.get_id());



                newPlanTask = planTaskRepo.findById(planTask.get_id()).orElse(null);
                List<Object> milestones = newPlanTask.getMilestones(); // original milestones
                if(milestones != null) {
                    for (Object milestone : milestones) {
                        if (!planTask.getMilestones().contains(milestone)) {
                            // this milestone is deleted
                            // delete all userProgress of this milestone
                            System.out.println("\u001B[42m PlanTask not countain " + planTask.get_id() + "\u001B[0m");
                            userProgressService.deleteAllBySubTaskId(plan.get_id(), (String)milestone);
                        }
                    }
                }
                if(newPlanTask.getPlanType() == PlanType.VIVA) {
                    List<Object> milestones1 = newPlanTask.getMilestones(); // original milestones
                    for (Object milestone : milestones1) {
                        if (!planTask.getMilestones().contains(milestone)) {
                            // this milestone is deleted
                            // delete all userProgress of this milestone
                            System.out.println("\u001B[42m PlanTask not countain " + planTask.get_id()+ "\u001B[0m");
                            userProgressService.deleteByMilestoneId(plan.get_id(), newPlanTask.get_id());
                        }
                    }
                }
                System.out.println("new Id 2 = " + newPlanTask.get_id());

            }
            Integer totalTask = 0;
            if(planTask.getMilestones() != null) {
                for (Object milestone : planTask.getMilestones()) {
                    Phase<Task> coursePhase = (Phase<Task>) getPhaseById((String) milestone);
                    totalTask += coursePhase.getTotalTasks();
                }
            }
            List<Plan> plans = newPlanTask.getPlans();
            plans.add(plan);
            newPlanTask.setPlans(plans);
            newPlanTask.setTotalTasks(totalTask);
            newPlanTask.setMentor(planTask.getMentorIds());
            newPlanTask.setMilestones(planTask.getMilestones());
            newPlanTask.setPlanType(planTask.getPlanType());
            newPlanTask.setPlan(planTask.getPlan());
            newPlanTask.setDate(planTask.getDate());
            newPlanTask.setEstimatedTime(planTask.getEstimatedTime());
            newPlanTask.setPhase(phase);
            createdPlanTasks.add(planTaskRepo.save(newPlanTask));
//            String id = (task.get_id() == null || task.get_id().isEmpty()) ? String.valueOf(new ObjectId()) : task.get_id();
//            task.set_id(id);
        }
        return createdPlanTasks;
    }

    public List<Task> createTasks(List<Task> tasks, Phase<Task> phase, Integer entityType) {
        List<Task> createdTasks = new ArrayList<>();
        for (Task task : tasks) {
            Task newTask = new Task();
            if(task.get_id() == null || task.get_id().isEmpty() || task.get_id().isBlank()){
                newTask.set_id(String.valueOf(new ObjectId()));
            }else {
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
        return createdTasks;
    }

    public List<SubTask> createSubTasks(List<SubTask> subTasks, Task task, Integer entityType, Phase<Task> phase) {
        List<SubTask> createdSubTasks = new ArrayList<>();
        for (SubTask subTask : subTasks) {
            SubTask newSubTask = new SubTask();
            if(subTask.get_id() == null || subTask.get_id().isEmpty() || subTask.get_id().isBlank()){
                newSubTask.set_id(String.valueOf(new ObjectId()));
                // new subtask is created
                // task count to plantask
                List<PlanTask> planTasks = planTaskRepo.findByMilestoneId(phase.get_id());
                for (PlanTask planTask : planTasks) {
                    planTask.setTotalTasks(planTask.getTotalTasks() + 1);
                    Integer estimatedTime = planTask.getEstimatedTimeInSeconds();
                    planTask.setEstimatedTimeInSeconds(estimatedTime + subTask.getEstimatedTimeInSeconds());
                    planTaskRepo.save(planTask);
                }
            }else {
                newSubTask = subTaskRepo.findById(subTask.get_id()).orElse(null);
            }
//            String id = (subTask.get_id() == null || subTask.get_id().isEmpty()) ? String.valueOf(new ObjectId()) : subTask.get_id();
//            subTask.set_id(id);
            newSubTask.setSubTask(subTask.getSubTask());
            newSubTask.setLink(subTask.getLink());
            newSubTask.setReference(subTask.getReference());
            newSubTask.setEstimatedTime(subTask.getEstimatedTime());
            newSubTask.setEntityType(entityType);
            newSubTask.setTask(task);
            createdSubTasks.add(subTaskRepo.save(newSubTask));
        }
        return createdSubTasks;
    }
    public Phase<?> getPhaseById(String phaseId) {
        Phase<?> phase = phaseRepo.findById(phaseId).orElse(null);
        return phase != null && !phase.getIsDeleted() ? phase : null;
    }


    public List<Phase> getPhaseByIds(List<String> phaseId) {
        return phaseRepo.findAllById(phaseId);
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
        return task.getIsDeleted() ? null : task;
    }
    public boolean deletePhase(Phase<T> phase) {
        if(phase != null) {
            List<PlanTask> planTasks = planTaskRepo.findByMilestoneId(phase.get_id());
            if(planTasks.size() > 0){
                return false;
            }
            phase.setIsDeleted(true);
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
            if(phase.getEntityType() == EntityType.TEST){
                Test test = (Test) phase.getEntity();
            }else if (phase.getEntityType() == EntityType.COURSE){
                Course course = (Course) phase.getEntity();
            }



//            planService.findIfPhaseExists(phase);

            phaseRepo.save(phase);
            return true;
        }
        return false;
    }
    public boolean deleteTask(Task task) {
        if(task != null){
            task.setIsDeleted(true);
            taskRepo.save(task);
            List<SubTask> subtasks = task.getSubtasks();
            if (subtasks != null) {
                for (SubTask subTask : subtasks) {
                    deleteSubtask(subTask);
//                    subTaskRepo.save(subTask);
                }
            }
            return true;
        }
        return false;
    }
    public boolean deleteTask(PlanTask task) {
        if(task != null){
            task.setIsDeleted(true);
            planTaskRepo.save(task);
            return true;
        }
        return false;
    }
    public boolean deleteSubtask(SubTask subTask){
        if(subTask != null){

            deleteUserProgressBySubTaskId(subTask.get_id());
            List<Task> taskList = findTasksBySubtask(subTask);
            System.out.println("\u001B[41m PhaseId" + taskList.size());

            for (Task task : taskList) {
               Phase<Task> phase = task.getPhase();
                System.out.println("\u001B[41m PhaseId" + phase.get_id());
                Integer phaseEstimatedTime = phase.getEstimatedTimeInSeconds();
                phase.setEstimatedTimeInSeconds(phaseEstimatedTime == 0 ? 0 : phaseEstimatedTime - subTask.getEstimatedTimeInSeconds());
               List<PlanTask> planTasks = planTaskRepo.findByMilestoneId(phase.get_id());
               for (PlanTask planTask : planTasks) {
                   System.out.println("\u001B[41m PlanTask" + planTask.getMentorDetails());
                   Integer estimatedTimeOfPhase = phase.getEstimatedTimeInSeconds();
                   estimatedTimeOfPhase = estimatedTimeOfPhase == 0 ? 0 : estimatedTimeOfPhase - subTask.getEstimatedTimeInSeconds();
                   phase.setEstimatedTimeInSeconds(estimatedTimeOfPhase);
                   planTask.setTotalTasks(planTask.getTotalTasks() == 0 ? 0 : planTask.getTotalTasks() - 1);
                   Integer estimatedTime = planTask.getEstimatedTimeInSeconds();
                   System.out.println("Estimated time in seconds: " + estimatedTime);
                   planTask.setEstimatedTimeInSeconds(estimatedTime == 0 ? 0 : estimatedTime - subTask.getEstimatedTimeInSeconds());
                   System.out.println("Estimated time in seconds: " + planTask.getEstimatedTime());

               }
            }
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
