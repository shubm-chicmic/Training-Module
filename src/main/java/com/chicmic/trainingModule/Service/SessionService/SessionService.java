package com.chicmic.trainingModule.Service.SessionService;

import com.chicmic.trainingModule.Dto.SessionDto;
import com.chicmic.trainingModule.Entity.MomMessage;
import com.chicmic.trainingModule.Repository.SessionRepo;
import com.chicmic.trainingModule.Util.CustomObjectMapper;
import com.chicmic.trainingModule.Entity.Session;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public List<Session> getAllSessions(Integer pageNumber, Integer pageSize, String query) {
        System.out.println("pageNumber = " + pageNumber);
        System.out.println("pageSize = " + pageSize);

        // Define the pageable object
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        // Create a query object with criteria for title search and isDeleted filtering
        Query searchQuery = new Query()
                .addCriteria(Criteria.where("title").regex(query, "i")) // Case-insensitive title search
                .addCriteria(Criteria.where("isDeleted").is(false))
                .with(pageable);

        // Fetch data based on the query
        List<Session> sessions = mongoTemplate.find(searchQuery, Session.class);

        System.out.println("Total sessions = " + sessions.size());
        return sessions;
    }
//    public long countNonDeletedSessions() {
//        // Custom aggregation query to count non-deleted sessions
//        return mongoTemplate.aggregate(
//                newAggregation(
//                        match(Criteria.where("isDeleted").is(false)),
//                        group().count().as("count")
//                ),
//                "session", // Replace 'session' with your collection name
//                CountResult.class // Define a class to hold the result count
//        ).getMappedResults().get(0).getCount();
//    }
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
    public long countNonDeletedSessions() {
        MatchOperation matchStage = Aggregation.match(Criteria.where("isDeleted").is(false));
        Aggregation aggregation = Aggregation.newAggregation(matchStage);
        AggregationResults<Session> aggregationResults = mongoTemplate.aggregate(aggregation, "session", Session.class);
        return aggregationResults.getMappedResults().size();
    }

    public Session postMOM(String sessionId,String message, String userId) {
        Session session = sessionRepo.findById(sessionId).orElse(null);
        MomMessage momMessage = new MomMessage();
        momMessage.set_id(userId);
        momMessage.setMessage(message);
        List<MomMessage> momMessages = session.getMOM();
        momMessages.add(momMessage);
        if (session != null) {
            session.setMOM(momMessages);
            sessionRepo.save(session);
            return session;
        } else {
            return null;
        }
    }

    public Session approve(Session session, String userId) {
        System.out.println("userId = " + userId + "sessionId = " + session.get_id());

        List<String> approvedBy = session.getApprovedBy();
        approvedBy.add(userId);
        session.setApprovedBy(approvedBy);
        session.setApproved(true);
        session.setStatus(2);
        return sessionRepo.save(session);
    }
}
