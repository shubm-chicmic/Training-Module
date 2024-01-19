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

    public UserTime createUserTime(UserTimeDto userTimeDto, String createdBy) {
        UserTime userTime = UserTime.builder()
                .createdBy(createdBy)
                .consumedTime(userTimeDto.getConsumedTime())
                .traineeId(userTimeDto.getTraineeId())
                .isDeleted(false)
                .planId(userTimeDto.getPlanId())
                .taskId(userTimeDto.getTaskId())
                .phaseId(userTimeDto.getPhaseId())
                .build();
        return saveUserTime(userTime);
    }
}
