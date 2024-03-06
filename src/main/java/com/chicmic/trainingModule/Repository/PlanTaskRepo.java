package com.chicmic.trainingModule.Repository;

import com.chicmic.trainingModule.Entity.Plan;
import com.chicmic.trainingModule.Entity.PlanTask;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface PlanTaskRepo extends MongoRepository<PlanTask, String> {
    @Query("{'planType': ?0, 'plan': ?1, 'milestones': ?2, 'isDeleted': false}")
    List<PlanTask> findByTypeAndPlanAndMilestoneId(Integer type, String plan, String milestoneId);

    @Query("{'milestones': ?0, 'isDeleted': false}")
    List<PlanTask> findByMilestoneId(String milestoneId);

    @Query("{'plan': ?0, 'isDeleted': false}")
    List<PlanTask> findByPlanId(String planId);
}
