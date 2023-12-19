package com.chicmic.trainingModule.Repository;

import com.chicmic.trainingModule.Entity.Test.Test;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TestRepo  extends MongoRepository<Test, String> {
}
