package com.chicmic.trainingModule.Service.SessionService;

import com.chicmic.trainingModule.Dto.SessionDto;
import com.chicmic.trainingModule.Dto.SessionResponseDto;
import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.chicmic.trainingModule.Repository.SessionRepo;
import com.chicmic.trainingModule.Util.CustomObjectMapper;
import com.chicmic.trainingModule.Entity.Session;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;
import java.util.Random;

import static com.mongodb.client.model.Aggregates.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;

@Service
@RequiredArgsConstructor
public class SessionService {
    private final SessionRepo sessionRepo;
    private final MongoTemplate mongoTemplate;
//    @Bean
//    private void clearSession(){
//        sessionRepo.deleteAll();
//    }
    public Session createSession(Session session){
        session = sessionRepo.save(session);
        return session;
    }
    public List<Session> getAllSessions(Integer pageNumber, Integer pageSize){
        System.out.println("pageNumber = " + pageNumber);
        System.out.println("pageSize = " + pageSize);
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        List<Session> sessions = sessionRepo.findAll(pageable).getContent();
        System.out.println("Total sessions = " + sessions.size());
        return sessions;
    }
    public long countNonDeletedSessions() {
        // Custom aggregation query to count non-deleted sessions
        return mongoTemplate.aggregate(
                newAggregation(
                        match(Criteria.where("isDeleted").is(false)),
                        group().count().as("count")
                ),
                "session", // Replace 'session' with your collection name
                CountResult.class // Define a class to hold the result count
        ).getMappedResults().get(0).getCount();
    }
    public Session getSessionById(String sessionId){
        return sessionRepo.findById(sessionId).orElse(null);
    }

    public Boolean deleteSessionById(String sessionId) {
        Session session = sessionRepo.findById(sessionId).orElse(null);
        if (session != null) {
            session.setDeleted(true);
            sessionRepo.save(session);
            return true;
        } else {
            return false;
        }
    }

    public Session updateStatus(String sessionId, int status) {
        Session session = sessionRepo.findById(sessionId).orElse(null);
        if (session != null) {
            session.setStatus(status);
            sessionRepo.save(session);
            return session;
        } else {
            return null;
        }
    }

    public Session updateSession(SessionDto sessionDto, String sessionId) {
        Session session = sessionRepo.findById(sessionId).orElse(null);
        if (session != null) {
            session = (Session) CustomObjectMapper.updateFields(sessionDto, session);
            sessionRepo.save(session);
            return session;
        } else {
            return null;
        }
    }

    public Session postMOM(String sessionId,String message) {
        Session session = sessionRepo.findById(sessionId).orElse(null);
        if (session != null) {
            session.setMOM(message);
            sessionRepo.save(session);
            return session;
        } else {
            return null;
        }
    }

    public void approve(Session session, String userId) {
        session.setApproved(true);
        List<String> approvedBy = session.getApprovedBy();
        approvedBy.add(userId);
        session.setApprovedBy(approvedBy);
        sessionRepo.save(session);
    }
}
