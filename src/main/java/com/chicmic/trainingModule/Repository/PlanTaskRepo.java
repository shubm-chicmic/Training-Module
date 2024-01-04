package com.chicmic.trainingModule.Repository;

import com.chicmic.trainingModule.Entity.Plan;
import com.chicmic.trainingModule.Entity.PlanTask;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PlanTaskRepo extends MongoRepository<PlanTask, String> {
}
