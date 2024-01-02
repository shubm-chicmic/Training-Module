package com.chicmic.trainingModule.Repository;

import com.chicmic.trainingModule.Entity.UserProgress;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserProgressRepo extends MongoRepository<UserProgress, String> {
    UserProgress findByUserIdAndProgressTypeAndId(String userId, Integer progressType, String id);
}
