package com.chicmic.trainingModule.Repository;

import com.chicmic.trainingModule.Entity.Session;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SessionRepo extends MongoRepository<Session, Long> {
    Page<Session> findAllBy(TextCriteria criteria, Pageable pageable);
}

