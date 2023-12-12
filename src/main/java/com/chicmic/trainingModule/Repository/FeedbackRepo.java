package com.chicmic.trainingModule.Repository;

import com.chicmic.trainingModule.Entity.Feedback;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FeedbackRepo extends MongoRepository<Feedback,String> {
}
