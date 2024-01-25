package com.chicmic.trainingModule.Repository;

import com.chicmic.trainingModule.Entity.Plan;
import com.chicmic.trainingModule.Entity.PlanTask;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface PlanTaskRepo extends MongoRepository<PlanTask, String> {
    @Query("{'milestones': ?0, 'isDeleted': false}")
    List<PlanTask> findByMilestoneId(String milestoneId);
    @Query("{'plan': ?0, 'isDeleted': false}")
    List<PlanTask> findByPlanId(String planId);
}
