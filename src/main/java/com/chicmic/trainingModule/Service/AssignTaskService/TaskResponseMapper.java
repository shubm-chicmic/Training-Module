package com.chicmic.trainingModule.Service.AssignTaskService;

import com.chicmic.trainingModule.Dto.AssignTaskDto.TaskDto;
import com.chicmic.trainingModule.Dto.CourseDto.CourseResponseDto;
import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.chicmic.trainingModule.Entity.Course;
import com.chicmic.trainingModule.Entity.SubTask;
import com.chicmic.trainingModule.Entity.Task;
import com.chicmic.trainingModule.Service.UserProgressService.UserProgressService;
import com.chicmic.trainingModule.TrainingModuleApplication;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskResponseMapper {
    private final UserProgressService userProgressService;
    public  List<TaskDto> mapTaskToResponseDto(List<Task> taskList, String traineeId) {
        List<TaskDto> result = new ArrayList<TaskDto>();
        for (Task task : taskList) {
            for (SubTask subTask : task.getSubtasks()){
                Boolean isSubTaskCompleted = userProgressService.findIsSubTaskCompleted(subTask.get_id(), traineeId);
                UserIdAndNameDto mainTask = UserIdAndNameDto.builder().name(task.getMainTask())._id(task.get_id()).build();
                TaskDto taskDto = TaskDto.builder()
                        .mainTask(mainTask)
                        .estimatedTime(subTask.getEstimatedTime())
                        .reference(subTask.getReference())
                        .subtask(subTask.getSubTask())
                        .subtaskId(subTask.get_id())
                        .isCompleted(isSubTaskCompleted)
                        .build();
                result.add(taskDto);
            }
        }
        return result;
    }
}
