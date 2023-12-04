package com.chicmic.trainingModule.trainingModule.Service;

import com.chicmic.trainingModule.trainingModule.Dto.SessionDto;
import com.chicmic.trainingModule.trainingModule.Entity.Session;
import com.chicmic.trainingModule.trainingModule.Repository.SessionRepo;
import com.chicmic.trainingModule.trainingModule.Util.CustomObjectMapper;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionService {
    private final SessionRepo sessionRepo;
    public Session createSession(Session session){
        session = sessionRepo.save(session);
        return session;
    }
    public List<Session> getAllSessions(){
        return sessionRepo.findAll();
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

    public Session updateStatus(Long sessionId, String status) {
        Session session = sessionRepo.findById(sessionId).orElse(null);
        if (session != null) {
            session.setStatus(status);
            sessionRepo.save(session);
            return session;
        } else {
            return null;
        }
    }

    public Session updateSession(SessionDto sessionDto) {
        Session session = sessionRepo.findById(sessionDto.getId()).orElse(null);
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
