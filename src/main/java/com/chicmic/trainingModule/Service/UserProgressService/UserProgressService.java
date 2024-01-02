package com.chicmic.trainingModule.Service.UserProgressService;

import com.chicmic.trainingModule.Entity.Constants.ProgessConstants;
import com.chicmic.trainingModule.Entity.UserProgress;
import com.chicmic.trainingModule.Repository.UserProgressRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserProgressService {
    private final UserProgressRepo userProgressRepo;
    public UserProgress createUserProgress(UserProgress userProgress) {
        return userProgressRepo.save(userProgress);
    }

    public Boolean findIsPlanCompleted(String plan, Integer planType, String userId) {
        UserProgress userProgress = userProgressRepo.findByUserIdAndProgressTypeAndId(userId, planType, plan);
        return userProgress.getStatus() == ProgessConstants.Completed;
    }

    public Boolean findIsSubTaskCompleted(String id, String userId) {
        UserProgress userProgress =  userProgressRepo.findByUserIdAndProgressTypeAndId(userId, 5, id);
        return userProgress.getStatus() == ProgessConstants.Completed;
    }
}
