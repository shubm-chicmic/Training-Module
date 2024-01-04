package com.chicmic.trainingModule.Repository;

import com.chicmic.trainingModule.Entity.UserProgress;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserProgressRepo extends MongoRepository<UserProgress, String> {
//    Optional<UserProgress> findByPlanIdAndCourseIdAndTraineeIdAndId(
//            String planId, String courseId, String traineeId, String id);
    Optional<UserProgress> findByTraineeIdAndPlanIdAndCourseIdAndProgressType(
            String traineeId, String planId, String courseId, Integer progressType);
    long countByTraineeIdAndPlanIdAndCourseIdAndProgressTypeAndStatus(
            String traineeId, String planId, String courseId, Integer progressType, Integer status);


    long countByTraineeIdAndPlanIdAndProgressTypeAndStatus(String traineeId, String planId, int progressType, int completed);
//    Optional<UserProgress> findByTraineeIdAndPlanIdAndCourseIdAndIdAndProgressType(
//            String traineeId, String planId, String courseId,String id, Integer progressType);
}
