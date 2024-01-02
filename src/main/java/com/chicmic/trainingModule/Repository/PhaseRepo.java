package com.chicmic.trainingModule.Repository;


import com.chicmic.trainingModule.Entity.Phase;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PhaseRepo extends MongoRepository<Phase, String> {
}
