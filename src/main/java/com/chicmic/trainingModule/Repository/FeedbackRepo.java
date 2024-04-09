package com.chicmic.trainingModule.Repository;

import com.chicmic.trainingModule.Entity.Course;
import com.chicmic.trainingModule.Entity.Feedback_V2;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedbackRepo extends MongoRepository<Feedback_V2, String> {
    List<Feedback_V2> findByMentorAndTypeAndCreatedByAndIsDeletedFalse(String mentor, String type, String createdBy);
    List<Feedback_V2> findByMentorAndTypeAndCreatedByAndCreatedAtBetweenAndIsDeletedFalse(String mentor, String type, String createdBy, Date startOfMonth, Date endOfMonth);
    List<Feedback_V2> findByMentorAndTypeAndIsDeletedFalse(String mentor, String type);
}
