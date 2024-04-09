package com.chicmic.trainingModule.Repository;

import com.chicmic.trainingModule.Entity.Course;
import com.chicmic.trainingModule.Entity.Feedback_V2;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface FeedbackRepo extends MongoRepository<Feedback_V2, String> {
    List<Feedback_V2> findByMentorAndTypeAndCreatedBy(String mentor, String type, String createdBy);
    List<Feedback_V2> findByMentorAndTypeAndCreatedByAndCreatedAtBetween(String mentor, String type, String createdBy, Date startOfMonth, Date endOfMonth);

    List<Feedback_V2> findByMentorAndType(String mentor, String type);
}
