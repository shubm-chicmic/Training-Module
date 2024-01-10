package com.chicmic.trainingModule.Service;

import com.chicmic.trainingModule.Entity.*;
import com.chicmic.trainingModule.Entity.Constants.EntityType;
import com.chicmic.trainingModule.Repository.PhaseRepo;
import com.chicmic.trainingModule.Repository.PlanTaskRepo;
import com.chicmic.trainingModule.Repository.SubTaskRepo;
import com.chicmic.trainingModule.Repository.TaskRepo;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.formula.functions.T;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PhaseService {
    private final PhaseRepo phaseRepo;
    private final TaskRepo taskRepo;
    private final SubTaskRepo subTaskRepo;
    private final PlanTaskRepo planTaskRepo;
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
            if(phase.get_id() == null || phase.get_id().isEmpty()){
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
            if(planTask.get_id() == null || planTask.get_id().isEmpty()){
                newPlanTask.set_id(String.valueOf(new ObjectId()));
            }else {
                newPlanTask = planTaskRepo.findById(planTask.get_id()).orElse(null);
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
            newPlanTask.setPlanType(EntityType.PLAN);
            newPlanTask.setPlan(planTask.getPlan());
            newPlanTask.setDate(planTask.getDate());
            newPlanTask.setEstimatedTime(planTask.getEstimatedTime());
            newPlanTask.setPhase(phase);
            createdPlanTasks.add(planTaskRepo.save(planTask));
//            String id = (task.get_id() == null || task.get_id().isEmpty()) ? String.valueOf(new ObjectId()) : task.get_id();
//            task.set_id(id);
        }
        return createdPlanTasks;
    }

    public List<Task> createTasks(List<Task> tasks, Phase<Task> phase, Integer entityType) {
        List<Task> createdTasks = new ArrayList<>();
        for (Task task : tasks) {
            Task newTask = new Task();
            if(task.get_id() == null || task.get_id().isEmpty()){
                newTask.set_id(String.valueOf(new ObjectId()));
            }else {
                newTask = taskRepo.findById(task.get_id()).orElse(null);
            }
//            String id = (task.get_id() == null || task.get_id().isEmpty()) ? String.valueOf(new ObjectId()) : task.get_id();
//            task.set_id(id);
            List<SubTask> subTasks = createSubTasks(task.getSubtasks(), newTask, entityType);
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

    public List<SubTask> createSubTasks(List<SubTask> subTasks, Task task, Integer entityType) {
        List<SubTask> createdSubTasks = new ArrayList<>();
        for (SubTask subTask : subTasks) {
            SubTask newSubTask = new SubTask();
            if(subTask.get_id() == null || subTask.get_id().isEmpty()){
                newSubTask.set_id(String.valueOf(new ObjectId()));
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
                    subTask.setIsDeleted(true);
                    subTaskRepo.save(subTask);
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
            subTask.setIsDeleted(true);
            subTaskRepo.save(subTask);
            return true;
        }
        return false;
    }

}
