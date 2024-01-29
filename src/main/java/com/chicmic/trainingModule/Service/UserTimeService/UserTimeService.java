package com.chicmic.trainingModule.Service.UserTimeService;

import com.chicmic.trainingModule.Dto.UserTimeDto.UserTimeDto;
import com.chicmic.trainingModule.Entity.UserTime;
import com.chicmic.trainingModule.Repository.UserTimeRepo;
import org.springframework.stereotype.Service;

@Service
public class UserTimeService {
    private final UserTimeRepo userTimeRepo;

    public UserTimeService(UserTimeRepo userTimeRepo) {
        this.userTimeRepo = userTimeRepo;
    }

    public UserTime saveUserTime(UserTime userTime) {
        return userTimeRepo.save(userTime);
    }

    public UserTime createUserTime(UserTimeDto userTimeDto, String traineeId) {
        UserTime userTime = UserTime.builder()
                .traineeId(traineeId)
                .consumedTime(userTimeDto.getConsumedTime())
                .isDeleted(false)
                .planId(userTimeDto.getPlanId())
                .planTaskId(userTimeDto.getPlanTaskId())
                .subTaskId(userTimeDto.getSubTaskId())
                .build();
        return saveUserTime(userTime);
    }
}
