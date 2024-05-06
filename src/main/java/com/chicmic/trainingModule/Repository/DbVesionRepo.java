package com.chicmic.trainingModule.Repository;

import com.chicmic.trainingModule.Entity.Course;
import com.chicmic.trainingModule.Entity.DbVersion;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DbVesionRepo extends MongoRepository<DbVersion, String> {
}
