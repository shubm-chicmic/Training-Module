package com.chicmic.trainingModule.Service.UserProgressService;

import com.chicmic.trainingModule.Entity.Constants.ProgessConstants;
import com.chicmic.trainingModule.Entity.UserProgress;
import com.chicmic.trainingModule.Repository.UserProgressRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserProgressService {
    private final UserProgressRepo userProgressRepo;
    public UserProgress createUserProgress(UserProgress userProgress) {
        return userProgressRepo.save(userProgress);
    }
    public UserProgress getUserProgress(String plan, Integer planType, String userId){
        return userProgressRepo.findByUserIdAndProgressTypeAndId(userId, planType, plan);
    }

    public Boolean findIsPlanCompleted(String plan, Integer planType, String userId) {
        UserProgress userProgress = userProgressRepo.findByUserIdAndProgressTypeAndId(userId, planType, plan);
        if(userProgress == null){
            return false;
        }
        return userProgress.getStatus() == ProgessConstants.Completed;
    }

    public Boolean findIsSubTaskCompleted(String id, String userId) {
        UserProgress userProgress =  userProgressRepo.findByUserIdAndProgressTypeAndId(userId, 5, id);
        if(userProgress == null){
            return false;
        }
        return userProgress.getStatus() == ProgessConstants.Completed;
    }

//    public long getTotalCompletedTasks(String traineeId, String planType, String id) {
//        Query query = new Query();
//        query.addCriteria(Criteria.where("userId").is(traineeId).and("progressType").is(planType).and("id").is(id));
//        return userProgressRepo.count(query);
//
//    }
    public Integer getTotalCompletedTasks(String traineeId, String planType, String id) {
        return null;
    }

}
