package com.chicmic.trainingModule.Repository;

import com.chicmic.trainingModule.Entity.Session;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface SessionRepo extends MongoRepository<Session, String> {
    Page<Session> findAllBy(TextCriteria criteria, Pageable pageable);
    @Query("{'isDeleted' : false}")
    long countNonDeletedSessions();
}

