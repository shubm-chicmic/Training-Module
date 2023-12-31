package com.chicmic.trainingModule.Service.SessionService;

import com.chicmic.trainingModule.Dto.SessionDto.SessionDto;
import com.chicmic.trainingModule.Entity.Course.Course;
import com.chicmic.trainingModule.Entity.Session.MomMessage;
import com.chicmic.trainingModule.Entity.StatusConstants;
import com.chicmic.trainingModule.Repository.SessionRepo;
import com.chicmic.trainingModule.Util.CustomObjectMapper;
import com.chicmic.trainingModule.Entity.Session.Session;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        session = sessionRepo.save(session);
        return session;
    }
    public List<Session> getAllSessions(Integer pageNumber, Integer pageSize, String query, Integer sortDirection, String sortKey, String userId) {
        System.out.println("pageNumber = " + pageNumber);
        System.out.println("pageSize = " + pageSize);
        System.out.println("query = " + query);
        System.out.println("sortDirection = " + sortDirection);
        System.out.println("sortKey = " + sortKey);
        Pageable pageable;
        if(!sortKey.isEmpty()) {
            Sort.Direction direction = (sortDirection == 0) ? Sort.Direction.ASC : Sort.Direction.DESC;
            Sort sort = Sort.by(direction, sortKey);
            pageable = PageRequest.of(pageNumber, pageSize, sort);
        }else {
           pageable = PageRequest.of(pageNumber, pageSize);
        }

        Criteria criteria = Criteria.where("title").regex(query, "i")
                .and("isDeleted").is(false);

        Criteria approvedCriteria = Criteria.where("isApproved").is(true)
                .andOperator(
                        new Criteria().orOperator(
                                Criteria.where("createdBy").is(userId),
                                Criteria.where("approver").in(userId),
                                Criteria.where("trainees").in(userId),
                                Criteria.where("sessionBy").in(userId)

                        )
                );
        Criteria reviewersCriteria = Criteria.where("isApproved").is(false)
                .and("approver").in(userId);
        Criteria createdByCriteria = Criteria.where("isApproved").is(false)
                .and("createdBy").is(userId);

        Criteria finalCriteria = new Criteria().andOperator(
                criteria,
                new Criteria().orOperator(approvedCriteria, reviewersCriteria, createdByCriteria)
        );

        Query searchQuery = new Query(finalCriteria).with(pageable);


//        // Create a query object with criteria for title search and isDeleted filtering
//        Query searchQuery = new Query()
//                .addCriteria(Criteria.where("title").regex(query, "i")) // Case-insensitive title search
//                .addCriteria(Criteria.where("isDeleted").is(false))
//                .with(pageable);

        // Fetch data based on the query and apply sorting by title
        List<Session> sessions = mongoTemplate.find(searchQuery, Session.class);
        if(!sortKey.isEmpty()) {
            Comparator<Session> sessionComparator = Comparator.comparing(session -> {
                try {
                    Field field = Session.class.getDeclaredField(sortKey);
                    field.setAccessible(true);
                    Object value = field.get(session);
                    if (value instanceof String) {
                        return ((String) value).toLowerCase();
                    }
                    return value.toString();
                } catch (Exception e) {
                    e.printStackTrace();
                    return "";
                }
            });

            if (sortDirection == 1) {
                sessions.sort(sessionComparator.reversed());
            } else {
                sessions.sort(sessionComparator);
            }
        }



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
            Integer count = 0;
            for (String reviewer : session.getApprover()){
                if(session.getApprovedBy().contains(reviewer)){
                    count++;
                }
            }
            if(count > 0){
                session.setApproved(true);
            }else {
                session.setApproved(false);
            }
            Set<String> approvedBy = new HashSet<>();
            for (String approver : session.getApprovedBy()){
                if(session.getApprover().contains(approver)){
                    approvedBy.add(approver);
                }
            }
            session.setApprovedBy(approvedBy);
            session.setUpdatedAt(LocalDateTime.now());
            sessionRepo.save(session);
            return session;
        } else {
            return null;
        }
    }
    public long countNonDeletedSessions(String query) {
        MatchOperation matchStage = Aggregation.match(Criteria.where("title").regex(query, "i")
                .and("isDeleted").is(false));

        Aggregation aggregation = Aggregation.newAggregation(matchStage);
        AggregationResults<Session> aggregationResults = mongoTemplate.aggregate(aggregation, "session", Session.class);
        return aggregationResults.getMappedResults().size();
    }

    public Session postMOM(String sessionId,String message, String userId) {
        Session session = sessionRepo.findById(sessionId).orElse(null);
        MomMessage momMessage = new MomMessage();
        momMessage.set_id(userId);
        momMessage.setMessage(message);

        if (session != null) {
            session.setMOM(momMessage);
            sessionRepo.save(session);
            return session;
        } else {
            return null;
        }
    }

    public Session approve(Session session, String userId, Boolean approvedValue) {
        System.out.println("userId = " + userId + "sessionId = " + session.get_id());

        Set<String> approvedBy = session.getApprovedBy();
        approvedBy.add(userId);
        session.setApprovedBy(approvedBy);
        session.setApproved(approvedValue);
        session.setStatus(StatusConstants.UPCOMING);
        return sessionRepo.save(session);
    }
}
