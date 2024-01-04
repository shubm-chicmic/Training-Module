package com.chicmic.trainingModule.Service.AssignTaskService;

import com.chicmic.trainingModule.Dto.AssignTaskDto.TaskDto;
import com.chicmic.trainingModule.Dto.CourseDto.CourseResponseDto;
import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.chicmic.trainingModule.Entity.Course;
import com.chicmic.trainingModule.Entity.SubTask;
import com.chicmic.trainingModule.Entity.Task;
import com.chicmic.trainingModule.Repository.SubTaskRepo;
import com.chicmic.trainingModule.Service.UserProgressService.UserProgressService;
import com.chicmic.trainingModule.TrainingModuleApplication;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskResponseMapper {
    private final UserProgressService userProgressService;
    private final SubTaskRepo subTaskRepo;

    public  List<TaskDto> mapTaskToResponseDto(List<Task> taskList,String planId, String courseId, String traineeId) {
        List<TaskDto> result = new ArrayList<TaskDto>();
        for (Task task : taskList) {
            for (SubTask subTask : task.getSubtasks()){
                Boolean isSubTaskCompleted = userProgressService.findIsSubTaskCompleted(planId, courseId, subTask.get_id(), traineeId);
                UserIdAndNameDto mainTask = UserIdAndNameDto.builder().name(task.getMainTask())._id(task.get_id()).build();
                UserIdAndNameDto subTaskIdAndName = UserIdAndNameDto.builder()
                        ._id(subTask.get_id())
                        .name(subTask.getSubTask())
                        .build();
                UserIdAndNameDto phaseDetails = UserIdAndNameDto.builder()
                        ._id(task.getPhase().get_id())
                        .name(task.getPhase().getName())
                        .build();
                TaskDto taskDto = TaskDto.builder()
                        .mainTask(mainTask)
                        .consumedTime("00:00")
                        .estimatedTime(subTask.getEstimatedTime())
                        .reference(subTask.getReference())
                        .subTask(subTaskIdAndName)
                        .phase(phaseDetails)
                        .isCompleted(isSubTaskCompleted)
                        .build();
                result.add(taskDto);
            }
        }
        return result;
    }
}
