package com.chicmic.trainingModule.Repository;

import com.chicmic.trainingModule.Entity.Course;
import com.chicmic.trainingModule.Entity.SubTask;
import com.chicmic.trainingModule.Entity.Task;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface TaskRepo extends MongoRepository<Task, String> {
    @Query("{ 'subtasks' : ?0 }")
    List<Task> findTasksBySubtask(SubTask subTask);
}
