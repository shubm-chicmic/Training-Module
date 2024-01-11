package com.chicmic.trainingModule.Repository;

import com.chicmic.trainingModule.Entity.UserProgress;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface UserProgressRepo extends MongoRepository<UserProgress, String> {
    //    Optional<UserProgress> findByPlanIdAndCourseIdAndTraineeIdAndId(
//            String planId, String courseId, String traineeId, String id);
//    Optional<UserProgress> findByTraineeIdAndPlanIdAndCourseIdAndProgressType(
//            String traineeId, String planId, String courseId, Integer progressType);

    long countByTraineeIdAndPlanIdAndCourseIdAndProgressTypeAndStatus(
            String traineeId, String planId, String courseId, Integer progressType, Integer status);


    long countByTraineeIdAndPlanIdAndProgressTypeAndStatus(String traineeId, String planId, int progressType, int completed);

    //    Optional<UserProgress> findByTraineeIdAndPlanIdAndCourseIdAndIdAndProgressType(
//            String traineeId, String planId, String courseId,String id, Integer progressType);
    List<UserProgress> findByTraineeIdAndPlanId(String traineeId, String planId);
    Optional<UserProgress> findByTraineeIdAndPlanIdAndCourseId(String traineeId, String planId, String courseId);
    List<UserProgress> findByTraineeId(String traineeId);
    Optional<UserProgress> findByTraineeIdAndPlanIdAndPlanTaskIdAndProgressType(
            String traineeId, String planId, String planTaskId, Integer progressType);
    Optional<UserProgress> findByTraineeIdAndPlanIdAndCourseIdAndProgressType(
            String traineeId, String planId, String courseId, Integer progressType);

    Optional<UserProgress> findByTraineeIdAndPlanIdAndProgressType(
            String traineeId, String planId, Integer progressType);
//    @Modifying
//    @Query("{ 'subTaskId' : ?0 }")
//    long deleteAllBySubTaskId(String subTaskId);


    List<UserProgress> findBySubTaskId(String subTaskId);


}
