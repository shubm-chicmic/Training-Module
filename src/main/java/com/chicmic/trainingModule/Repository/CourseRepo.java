package com.chicmic.trainingModule.Repository;

import com.chicmic.trainingModule.Entity.Course.Course;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CourseRepo extends MongoRepository<Course, String> {
}
