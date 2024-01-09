package com.chicmic.trainingModule.Service;

import com.chicmic.trainingModule.Entity.*;
import com.chicmic.trainingModule.Entity.Constants.EntityType;
import com.chicmic.trainingModule.Repository.PhaseRepo;
import com.chicmic.trainingModule.Repository.SubTaskRepo;
import com.chicmic.trainingModule.Repository.TaskRepo;
import lombok.RequiredArgsConstructor;
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
    public List<Phase<Task>> createPhases(List<Phase<Task>> phases, Object entity, Integer entityType) {
        int count = 0;
        List<Phase<Task>> createdPhases = new ArrayList<>();
        List<Phase<Task>> entityPhases = new ArrayList<>();
        if(entityType == EntityType.COURSE){
            entityPhases = ((Course) entity).getPhases();
        }else if (entityType == EntityType.TEST) {
            entityPhases = ((Test) entity).getMilestones();
        }
        for (Phase<Task> phase : phases) {
            if(phase.get_id() == null || phase.get_id().isEmpty()){
                phase.set_id(String.valueOf(new ObjectId()));
            }else {
                phase = getPhaseById(phase.get_id());
            }
            count++;
            List<Task> tasks = createTasks(phase.getTasks(), phase, entityType);
            phase.setName("Phase " + count);
            phase.setEntityType(entityType);
            phase.setTasks(tasks);
            phase.setEntity(entity);
            createdPhases.add(phaseRepo.save(phase));
        }
        //Delete phases
        int i = 0, j = 0;
        while(i < entityPhases.size() && j < createdPhases.size()){
            if(entityPhases.get(i).get_id().equals(createdPhases.get(j).get_id())){
                i++;
                j++;
            }else {
                Phase<Task> phase = entityPhases.get(i);
                phase.setIsDeleted(true);
                phaseRepo.save(phase);
                i++;
            }
        }
        return createdPhases;
    }

    public List<Task> createTasks(List<Task> tasks, Phase<Task> phase, Integer entityType) {
        List<Task> createdTasks = new ArrayList<>();
        for (Task task : tasks) {
            String id = (task.get_id() == null || task.get_id().isEmpty()) ? String.valueOf(new ObjectId()) : task.get_id();
            task.set_id(id);
            List<SubTask> subTasks = createSubTasks(task.getSubtasks(), task, entityType);
            task.setEntityType(entityType);
            task.setSubtasks(subTasks);
            task.setPhase(phase);
            createdTasks.add(taskRepo.save(task));
        }
        return createdTasks;
    }

    public List<SubTask> createSubTasks(List<SubTask> subTasks, Task task, Integer entityType) {
        List<SubTask> createdSubTasks = new ArrayList<>();
        for (SubTask subTask : subTasks) {
            String id = (subTask.get_id() == null || subTask.get_id().isEmpty()) ? String.valueOf(new ObjectId()) : subTask.get_id();
            subTask.set_id(id);
            subTask.setEntityType(entityType);
            subTask.setTask(task);
            createdSubTasks.add(subTaskRepo.save(subTask));
        }
        return createdSubTasks;
    }
    public Phase<Task> getPhaseById(String phaseId) {
        return phaseRepo.findById(phaseId).orElse(null);
    }

    public List<Phase> getPhaseByIds(List<String> phaseId) {
        return phaseRepo.findAllById(phaseId);
    }

}
