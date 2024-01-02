package com.chicmic.trainingModule.Repository;

import com.chicmic.trainingModule.Entity.AssignedPlan;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AssignTaskRepo extends MongoRepository<AssignedPlan, String> {
}
