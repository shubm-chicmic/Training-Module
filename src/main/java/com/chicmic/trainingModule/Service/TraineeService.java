package com.chicmic.trainingModule.Service;

import com.chicmic.trainingModule.Dto.UserDto;
import com.chicmic.trainingModule.TrainingModuleApplication;
import org.springframework.stereotype.Service;

@Service
public class TraineeService {

    public static boolean isUserInSameTeam(UserDto user1, UserDto user2) {
        if(user1 == null || user2 == null) {
            return false;
        }
        for (String team : user1.getTeams()) {
            if (user2.getTeams().contains(team)) {
                return true;
            }
        }
        return false;
    }
}
