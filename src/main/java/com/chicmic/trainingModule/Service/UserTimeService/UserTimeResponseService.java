package com.chicmic.trainingModule.Service.UserTimeService;

import com.chicmic.trainingModule.Dto.SessionDto.SessionResponseDto;
import com.chicmic.trainingModule.Dto.UserDto;
import com.chicmic.trainingModule.Dto.UserTimeDto.TaskTimeDto;
import com.chicmic.trainingModule.Dto.UserTimeDto.UserTimeResponseDto;
import com.chicmic.trainingModule.Entity.Session;
import com.chicmic.trainingModule.Entity.SubTask;
import com.chicmic.trainingModule.Entity.UserTime;
import com.chicmic.trainingModule.Service.PhaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserTimeResponseService {
    private final UserTimeService userTimeService;
    private final PhaseService phaseService;

    public List<UserTimeResponseDto> mapUserTimeToResponseDto(List<String> traineeData) {
        List<UserTimeResponseDto> userTimeResponseDtos = new ArrayList<>();
        for (String keyTraineeId : traineeData) {
            List<UserTime> userTimeList = userTimeService.getUserTimeByTraineeId(keyTraineeId);
            UserTimeResponseDto traineeTimeResponse = mapUserTimeToResponseDto(keyTraineeId, userTimeList);
            userTimeResponseDtos.add(traineeTimeResponse);
        }
        return userTimeResponseDtos;
    }

    public UserTimeResponseDto mapUserTimeToResponseDto(String traineeId, List<UserTime> userTimeList) {
        List<TaskTimeDto> taskTimeDto = new ArrayList<>();
        for (UserTime userTime : userTimeList) {
            if (userTime.getSubTaskId() != null && !userTime.getSubTaskId().isEmpty()) {
                SubTask subTask = phaseService.getSubTaskById(userTime.getSubTaskId());
                if (subTask == null) continue;
                TaskTimeDto taskTime = TaskTimeDto.builder()
                        .taskId(userTime.getSubTaskId())
                        .consumedTime(userTime.getConsumedTime())
                        .estimatedTime(subTask.getEstimatedTimeInSeconds())
                        .build();
                taskTimeDto.add(taskTime);
            }
        }
        return UserTimeResponseDto.builder()
                ._id(traineeId)
                .tasks(taskTimeDto)
                .totalTasks(taskTimeDto.size())
                .build();
    }
}
