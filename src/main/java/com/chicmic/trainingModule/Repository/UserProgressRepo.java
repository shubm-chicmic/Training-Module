package com.chicmic.trainingModule.Repository;

import com.chicmic.trainingModule.Entity.UserProgress;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserProgressRepo extends MongoRepository<UserProgress, String> {
    Optional<UserProgress> findByPlanIdAndCourseIdAndTraineeIdAndId(
            String planId, String courseId, String traineeId, String id);
    Optional<UserProgress> findByTraineeIdAndPlanIdAndCourseIdAndProgressType(
            String traineeId, String planId, String courseId, Integer progressType);
    long countByTraineeIdAndPlanIdAndCourseIdAndProgressType(
            String traineeId, String planId, String courseId, Integer progressType);

}
