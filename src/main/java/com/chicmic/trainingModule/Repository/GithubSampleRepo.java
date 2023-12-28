package com.chicmic.trainingModule.Repository;

import com.chicmic.trainingModule.Entity.GithubSample;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GithubSampleRepo extends MongoRepository<GithubSample, String> {
}
