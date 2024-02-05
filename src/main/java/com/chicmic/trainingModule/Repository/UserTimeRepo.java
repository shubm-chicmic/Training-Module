package com.chicmic.trainingModule.Repository;

import com.chicmic.trainingModule.Entity.UserTime;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface UserTimeRepo  extends MongoRepository<UserTime, String> {
    List<UserTime> findByTraineeId(String traineeId);

    // Find all UserTime records for a specific trainee and plan
    List<UserTime> findByTraineeIdAndPlanId(String traineeId, String planId);

    // Find all UserTime records for a specific trainee, plan, and plan task
    List<UserTime> findByTraineeIdAndPlanIdAndPlanTaskId(String traineeId, String planId, String planTaskId);

    // Find all UserTime records for a specific trainee, plan, plan task, and subtask
    List<UserTime> findByTraineeIdAndPlanIdAndPlanTaskIdAndSubTaskId(String traineeId, String planId, String planTaskId, String subTaskId);
}
