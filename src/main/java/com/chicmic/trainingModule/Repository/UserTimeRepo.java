package com.chicmic.trainingModule.Repository;

import com.chicmic.trainingModule.Entity.UserTime;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserTimeRepo  extends MongoRepository<UserTime, String> {
}
