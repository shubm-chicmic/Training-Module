package com.chicmic.trainingModule.Repository;

import com.chicmic.trainingModule.Entity.AssignTask.AssignTask;
import com.chicmic.trainingModule.Entity.Course.Course;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AssignTaskRepo extends MongoRepository<AssignTask, String> {
}
