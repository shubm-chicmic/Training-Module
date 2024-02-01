package com.chicmic.trainingModule.Service.SessionService;

import com.chicmic.trainingModule.Dto.SessionDto.SessionAttendDto;
import com.chicmic.trainingModule.Dto.SessionDto.SessionDto;
import com.chicmic.trainingModule.Dto.SessionDto.UserIdAndSessionStatusDto;
import com.chicmic.trainingModule.Entity.Constants.SessionAttendedStatus;
import com.chicmic.trainingModule.Entity.MomMessage;
import com.chicmic.trainingModule.Entity.Constants.StatusConstants;
import com.chicmic.trainingModule.ExceptionHandling.ApiException;
import com.chicmic.trainingModule.Repository.SessionRepo;
import com.chicmic.trainingModule.Entity.Session;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Collation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import org.springframework.security.core.GrantedAuthority;


@Service
@RequiredArgsConstructor
public class SessionService {
    private final SessionRepo sessionRepo;
    private final MongoTemplate mongoTemplate;
//    @Bean
//    private void clearSession(){
//        sessionRepo.deleteAll();
//    }
    public Session createSession(SessionDto sessionDto){
        Set<UserIdAndSessionStatusDto> trainees = new HashSet<>();
        for (String userId : sessionDto.getTrainees()) {
            UserIdAndSessionStatusDto userIdAndSessionStatusDto = new UserIdAndSessionStatusDto();
            userIdAndSessionStatusDto.set_id(userId);
            userIdAndSessionStatusDto.setAttendanceStatus(SessionAttendedStatus.PENDING);
            trainees.add(userIdAndSessionStatusDto);
        }
        Session session = Session.builder()
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .dateTime(sessionDto.getDateTime())
                .sessionBy(sessionDto.getSessionBy())
                .title(sessionDto.getTitle())
                .teams(sessionDto.getTeams())
                .createdBy(sessionDto.getCreatedBy())
                .trainees(trainees)
                .status(sessionDto.getStatus())
                .approver(sessionDto.getApprover())
                .isDeleted(false)
                .isApproved(false)
                .location(sessionDto.getLocation())
                .build();
        session = sessionRepo.save(session);
        return session;
    }
    public List<Session> getAllSessions(Integer pageNumber, Integer pageSize, String query, Integer sortDirection, String sortKey, String userId) {
        Boolean isRolePermit;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        isRolePermit = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("PA") || role.equals("PM") || role.equals("TL"));

        System.out.println("getAllSessions role permission " + isRolePermit);
        System.out.println("Authorities " + authentication.getAuthorities());
        System.out.println("pageNumber = " + pageNumber);
        System.out.println("pageSize = " + pageSize);
        System.out.println("query = " + query);
        System.out.println("sortDirection = " + sortDirection);
        System.out.println("sortKey = " + sortKey);
        Pageable pageable;
        pageable = PageRequest.of(pageNumber, pageSize);


        Criteria criteria = Criteria.where("title").regex(query, "i")
                .and("isDeleted").is(false);

        Criteria approvedCriteria = Criteria.where("isApproved").is(true)
                .andOperator(
                        new Criteria().orOperator(
                                Criteria.where("createdBy").is(userId),
                                Criteria.where("approver").in(userId),
                                Criteria.where("trainees._id").is(userId),
                                Criteria.where("sessionBy").in(userId)
                        )
                );
        Criteria reviewersCriteria = Criteria.where("isApproved").is(false)
                .and("approver").in(userId);
        Criteria createdByCriteria = Criteria.where("isApproved").is(false)
                .and("createdBy").is(userId);
        if(isRolePermit){
           approvedCriteria = Criteria.where("isApproved").is(true);
        }
        Criteria finalCriteria = new Criteria().andOperator(
                criteria,
                new Criteria().orOperator(approvedCriteria, reviewersCriteria, createdByCriteria)
        );
        Collation collation = Collation.of(Locale.ENGLISH).strength(Collation.ComparisonLevel.secondary());
        Query searchQuery = new Query(finalCriteria).with(pageable).collation(collation).with(Sort.by(sortDirection == 1 ? Sort.Direction.ASC : Sort.Direction.DESC, sortKey));;


//        // Create a query object with criteria for title search and isDeleted filtering
//        Query searchQuery = new Query()
//                .addCriteria(Criteria.where("title").regex(query, "i")) // Case-insensitive title search
//                .addCriteria(Criteria.where("isDeleted").is(false))
//                .with(pageable);

        // Fetch data based on the query and apply sorting by title
        List<Session> sessions = mongoTemplate.find(searchQuery, Session.class);
//        if(!sortKey.isEmpty()) {
//            Comparator<Session> sessionComparator = Comparator.comparing(session -> {
//                try {
//                    Field field = Session.class.getDeclaredField(sortKey);
//                    field.setAccessible(true);
//                    Object value = field.get(session);
//                    if (value instanceof String) {
//                        return ((String) value).toLowerCase();
//                    }
//                    return value.toString();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    return "";
//                }
//            });
//
//            if (sortDirection != 1) {
//                sessions.sort(sessionComparator.reversed());
//            } else {
//                sessions.sort(sessionComparator);
//            }
//        }



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

    public Boolean deleteSessionById(String sessionId,String name) {
        Session session = sessionRepo.findById(sessionId).orElse(null);
        Boolean isRolePermit;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        isRolePermit = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("PA") || role.equals("PM") || role.equals("TL"));
        System.out.println("Role Permit : " + isRolePermit);
        if((!session.getSessionBy().contains(name) || !session.getApprover().contains(name) || !session.getCreatedBy().equals(name)) && !isRolePermit)
            throw new ApiException(HttpStatus.BAD_REQUEST,"You are not Authorize to delete this session");
            //return new ApiResponse(HttpStatus.OK.value(), "Session deleted successfully", null);
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
            if(sessionDto.getTitle() != null){
                session.setTitle(sessionDto.getTitle());
            }
            if(sessionDto.getDateTime() != null){
                session.setDateTime(sessionDto.getDateTime());
            }
            if(sessionDto.getLocation() != null){
                session.setLocation(sessionDto.getLocation());
            }
            if(sessionDto.getTeams() != null){
                session.setTeams(sessionDto.getTeams());
            }
            if(sessionDto.getTrainees() != null) {
                List<String> newtrainees = sessionDto.getTrainees();
                Set<UserIdAndSessionStatusDto> originalTraineeStatus = session.getTraineesDetailsWithStatus();
                Set<String> originalTraineeIds = originalTraineeStatus.stream()
                        .map(UserIdAndSessionStatusDto::get_id)
                        .collect(Collectors.toSet());
                Set<UserIdAndSessionStatusDto> newTraineesSet = new HashSet<>();
                Map<String, UserIdAndSessionStatusDto> originalTraineeMap = originalTraineeStatus.stream()
                        .collect(Collectors.toMap(UserIdAndSessionStatusDto::get_id, Function.identity()));

                for (String newTrainee : newtrainees) {
                    if(!originalTraineeIds.contains(newTrainee)){
                        UserIdAndSessionStatusDto newTraineeWithStatus = new UserIdAndSessionStatusDto();
                        newTraineeWithStatus.setAttendanceStatus(SessionAttendedStatus.PENDING);
                        newTraineeWithStatus.set_id(newTrainee);
                        newTraineesSet.add(newTraineeWithStatus);
                    }else {
                        newTraineesSet.add(originalTraineeMap.get(newTrainee));
                    }
                }
                session.setTrainees(newTraineesSet);
            }
            if(sessionDto.getSessionBy() != null){
                session.setSessionBy(sessionDto.getSessionBy());
            }
            if(sessionDto.getApprover() != null) {
                session.setApprover(sessionDto.getApprover());
                Integer count = 0;
                for (String reviewer : session.getApprover()) {
                    if (session.getApprovedBy().contains(reviewer)) {
                        count++;
                    }
                }
                if (count > 0) {
                    session.setApproved(true);
                } else {
                    session.setApproved(false);
                    session.setStatus(StatusConstants.PENDING);
                }
                Set<String> approvedBy = new HashSet<>();
                for (String approver : session.getApprovedBy()) {
                    if (session.getApprover().contains(approver)) {
                        approvedBy.add(approver);
                    }
                }
                session.setApprovedBy(approvedBy);
            }
            session.setUpdatedAt(LocalDateTime.now());
            sessionRepo.save(session);
            return session;
        } else {
            return null;
        }
    }
    public long countNonDeletedSessions(String query, String userId) {
        System.out.println("countNonDeletedSessions " + query + " " + userId);
        Boolean isRolePermit;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        isRolePermit = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("PA") || role.equals("PM") || role.equals("TL"));


        Criteria criteria = Criteria.where("title").regex(query, "i")
                .and("isDeleted").is(false);

        Criteria approvedCriteria = Criteria.where("isApproved").is(true)
                .andOperator(
                        new Criteria().orOperator(
                                Criteria.where("createdBy").is(userId),
                                Criteria.where("approver").in(userId),
                                Criteria.where("trainees").elemMatch(
                                        Criteria.where("_id").is(userId)
                                ),
                                Criteria.where("sessionBy").in(userId)

                        )
                );
        Criteria reviewersCriteria = Criteria.where("isApproved").is(false)
                .and("approver").in(userId);
        Criteria createdByCriteria = Criteria.where("isApproved").is(false)
                .and("createdBy").is(userId);
        if(isRolePermit){
            approvedCriteria = Criteria.where("isApproved").is(true);
        }
        Criteria finalCriteria = new Criteria().andOperator(
                criteria,
                new Criteria().orOperator(approvedCriteria, reviewersCriteria, createdByCriteria)
        );

        Query searchQuery = new Query(finalCriteria);
        List<Session> sessions = mongoTemplate.find(searchQuery, Session.class);
        System.out.println("Session size = " + sessions.size());
        MatchOperation matchStage = Aggregation.match(finalCriteria);

        Aggregation aggregation = Aggregation.newAggregation(matchStage);
        AggregationResults<Session> aggregationResults = mongoTemplate.aggregate(aggregation, "session", Session.class);
        System.out.println("aggregationResults.getMappedResults().size() = " + aggregationResults.getMappedResults().size());
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

    public Session attendedSession(Session session, String userId, SessionAttendDto sessionAttendDto) {
        if(session == null)return null;
        Integer attendanceStatus = sessionAttendDto.getAttendanceStatus();
        Set<UserIdAndSessionStatusDto> attendedTrainees = session.getTraineesDetailsWithStatus();

        Optional<UserIdAndSessionStatusDto> existingUser = attendedTrainees.stream()
                .filter(user -> userId.equals(user.get_id()))
                .findFirst();
        if (existingUser.isPresent()) {
            if(attendanceStatus == SessionAttendedStatus.NOT_ATTENDED){
                existingUser.get().setReason(sessionAttendDto.getReason());
            }
            existingUser.get().setAttendanceStatus(attendanceStatus);
        } else {
            if(attendanceStatus == SessionAttendedStatus.NOT_ATTENDED){
                attendedTrainees.add(new UserIdAndSessionStatusDto(userId, attendanceStatus, sessionAttendDto.getReason()));
            }else {
                attendedTrainees.add(new UserIdAndSessionStatusDto(userId, attendanceStatus));
            }
        }
        System.out.println("Attended trainee = " + attendedTrainees);
        session.setTrainees(attendedTrainees);
        return sessionRepo.save(session);
    }
    public long countTotalAttendedSessionsByUser(String userId) {
        System.out.println("\u001B[33m Trainee Id = " + userId + "\u001B[0m");

        Criteria criteria = Criteria.where("isDeleted").is(false)
                .and("trainees").elemMatch(
                        Criteria.where("_id").is(userId)
                                .and("attendanceStatus").is(SessionAttendedStatus.ATTENDED)
                );

        MatchOperation matchStage = Aggregation.match(criteria);

        Aggregation aggregation = Aggregation.newAggregation(matchStage);
        AggregationResults<Session> aggregationResults = mongoTemplate.aggregate(aggregation, "session", Session.class);
        return aggregationResults.getMappedResults().size();
    }

    public long countTotalSessionsForUser(String userId) {
        System.out.println("\u001B[33m Trainee Id = " + userId + "\u001B[0m");
        Criteria criteria = Criteria.where("isDeleted").is(false)
                .and("trainees").elemMatch(
                        Criteria.where("_id").is(userId)
                );

        MatchOperation matchStage = Aggregation.match(criteria);

        Aggregation aggregation = Aggregation.newAggregation(matchStage);
        AggregationResults<Session> aggregationResults = mongoTemplate.aggregate(aggregation, "session", Session.class);
        return aggregationResults.getMappedResults().size();
    }

}
