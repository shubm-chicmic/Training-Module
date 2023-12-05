package com.chicmic.trainingModule.Service.SessionService;

import com.chicmic.trainingModule.Dto.SessionDto;
import com.chicmic.trainingModule.Repository.SessionRepo;
import com.chicmic.trainingModule.Util.CustomObjectMapper;
import com.chicmic.trainingModule.Entity.Session;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class SessionService {
    private final SessionRepo sessionRepo;
    @Bean
    private void clearSession(){
        sessionRepo.deleteAll();
    }
    public Session createSession(Session session){
        session = sessionRepo.save(session);
        return session;
    }
    public List<Session> getAllSessions(Integer pageNumber, Integer pageSize){
        System.out.println("pageNumber = " + pageNumber);
        System.out.println("pageSize = " + pageSize);
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        List<Session> sessions = sessionRepo.findAll(pageable).getContent();
        return sessions;
    }
    public Session getSessionById(Long sessionId){
        return sessionRepo.findById(sessionId).orElse(null);
    }

    public Boolean deleteSessionById(Long sessionId) {
        Session session = sessionRepo.findById(sessionId).orElse(null);
        if (session != null) {
            session.setDeleted(false);
            sessionRepo.save(session);
            return true;
        } else {
            return false;
        }
    }

    public Session updateStatus(Long sessionId, Long status) {
        Session session = sessionRepo.findById(sessionId).orElse(null);
        if (session != null) {
            session.setStatus("");
            sessionRepo.save(session);
            return session;
        } else {
            return null;
        }
    }

    public Session updateSession(SessionDto sessionDto, Long sessionId) {
        Session session = sessionRepo.findById(sessionId).orElse(null);
        if (session != null) {
            session = (Session) CustomObjectMapper.updateFields(sessionDto, session);
            sessionRepo.save(session);
            return session;
        } else {
            return null;
        }
    }

    public Session postMOM(Long sessionId,String message) {
        Session session = sessionRepo.findById(sessionId).orElse(null);
        if (session != null) {
            session.setMOM(message);
            sessionRepo.save(session);
            return session;
        } else {
            return null;
        }
    }
}
