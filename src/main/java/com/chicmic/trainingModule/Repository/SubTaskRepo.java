package com.chicmic.trainingModule.Repository;

import com.chicmic.trainingModule.Entity.Course;
import com.chicmic.trainingModule.Entity.SubTask;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SubTaskRepo extends MongoRepository<SubTask, String> {
}
