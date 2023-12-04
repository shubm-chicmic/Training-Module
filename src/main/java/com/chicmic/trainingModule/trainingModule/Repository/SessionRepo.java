package com.chicmic.trainingModule.trainingModule.Repository;

import com.chicmic.trainingModule.trainingModule.Entity.Session;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SessionRepo extends MongoRepository<Session, Long> {
}
