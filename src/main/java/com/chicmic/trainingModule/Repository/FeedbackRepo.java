package com.chicmic.trainingModule.Repository;

import com.chicmic.trainingModule.Entity.Feedback;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface FeedbackRepo extends MongoRepository<Feedback,String> {
    List<Feedback> findAllByTraineeID(String traineeId);
}
