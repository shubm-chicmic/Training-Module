package com.chicmic.trainingModule.Repository;

import com.chicmic.trainingModule.Entity.Course.Course;
import com.chicmic.trainingModule.Entity.Plan.Plan;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PlanRepo extends MongoRepository<Plan, String> {
}
