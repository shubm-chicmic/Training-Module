package com.chicmic.trainingModule.Repository;

import com.chicmic.trainingModule.Entity.Course;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepo extends MongoRepository<Course, String> {
}
