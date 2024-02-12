package com.chicmic.trainingModule.Repository;

import com.chicmic.trainingModule.Entity.UserTime;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserTimeRepo  extends MongoRepository<UserTime, String> {
    Optional<UserTime> findByPlanIdAndPlanTaskIdAndSubTaskId(String planId, String planTaskId, String subTaskId);

//    List<UserTime> findByTraineeId(String traineeId);
//
//    // Find all UserTime records for a specific trainee and plan
//    List<UserTime> findByTraineeIdAndPlanId(String traineeId, String planId);
//
//    // Find all UserTime records for a specific trainee, plan, and plan task
//    List<UserTime> findByTraineeIdAndPlanIdAndPlanTaskId(String traineeId, String planId, String planTaskId);
//
//    // Find all UserTime records for a specific trainee, plan, plan task, and subtask
//    List<UserTime> findByTraineeIdAndPlanIdAndPlanTaskIdAndSubTaskId(String traineeId, String planId, String planTaskId, String subTaskId);
    @Query("{'traineeId' : ?0, 'type' : { $nin: [ ?1, ?2 ] } }")
    List<UserTime> findByTraineeId(String traineeId, Integer vivaType, Integer pptType);

    // Find all UserTime records for a specific trainee and plan excluding VIVA and PPT types
    @Query("{'traineeId' : ?0, 'planId' : ?1, 'type' : { $nin: [ ?2, ?3 ] } }")
    List<UserTime> findByTraineeIdAndPlanId(String traineeId, String planId, Integer vivaType, Integer pptType);

    // Find all UserTime records for a specific trainee, plan, and plan task excluding VIVA and PPT types
    @Query("{'traineeId' : ?0, 'planId' : ?1, 'planTaskId' : ?2, 'type' : { $nin: [ ?3, ?4 ] } }")
    List<UserTime> findByTraineeIdAndPlanIdAndPlanTaskId(String traineeId, String planId, String planTaskId, Integer vivaType, Integer pptType);
    @Query("{'traineeId' : ?0, 'planId' : ?1, 'planTaskId' : ?2, 'type' : { $in: [ ?3, ?4 ] } }")
    UserTime findByTraineeIdAndPlanIdAndPlanTaskIdForVivaAndPPT(String traineeId, String planId, String planTaskId, Integer vivaType, Integer pptType);

    // Find all UserTime records for a specific trainee, plan, plan task, and subtask excluding VIVA and PPT types
    @Query("{'traineeId' : ?0, 'planId' : ?1, 'planTaskId' : ?2, 'subTaskId' : ?3, 'type' : { $nin: [ ?4, ?5 ] } }")
    List<UserTime> findByTraineeIdAndPlanIdAndPlanTaskIdAndSubTaskId(String traineeId, String planId, String planTaskId, String subTaskId, Integer vivaType, Integer pptType);
    Optional<UserTime> findBySessionIdAndType(String sessionId, Integer type);
}
