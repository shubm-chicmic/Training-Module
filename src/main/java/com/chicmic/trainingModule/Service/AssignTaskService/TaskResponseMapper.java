package com.chicmic.trainingModule.Service.AssignTaskService;

import com.chicmic.trainingModule.Dto.AssignTaskDto.TaskDto;
import com.chicmic.trainingModule.Dto.CourseDto.CourseResponseDto;
import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.chicmic.trainingModule.Entity.Course;
import com.chicmic.trainingModule.Entity.SubTask;
import com.chicmic.trainingModule.Entity.Task;
import com.chicmic.trainingModule.TrainingModuleApplication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TaskResponseMapper {
    public static List<TaskDto> mapTaskToResponseDto(List<Task> taskList) {
        List<TaskDto> result = new ArrayList<TaskDto>();
        for (Task task : taskList) {
            for (SubTask subTask : task.getSubtasks()){
                UserIdAndNameDto mainTask = UserIdAndNameDto.builder().name(task.getMainTask())._id(task.get_id()).build();
                TaskDto taskDto = TaskDto.builder()
                        .mainTask(mainTask)
                        .estimatedTime(subTask.getEstimatedTime())
                        .reference(subTask.getReference())
                        .subtask(subTask.getSubTask())
                        .subtaskId(subTask.get_id())
                        .isCompleted(false)
                        .build();
                result.add(taskDto);
            }
        }
        return result;
    }
}
