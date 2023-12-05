package com.chicmic.trainingModule.Repository;

import com.chicmic.trainingModule.Entity.GithubSample;
import com.chicmic.trainingModule.Entity.Session;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GithubSampleRepo extends MongoRepository<GithubSample, Long> {
}
