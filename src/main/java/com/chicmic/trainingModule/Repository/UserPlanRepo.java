package com.chicmic.trainingModule.Repository;

import com.chicmic.trainingModule.Entity.Plan.UserPlan;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserPlanRepo extends MongoRepository<UserPlan,String> {
}
