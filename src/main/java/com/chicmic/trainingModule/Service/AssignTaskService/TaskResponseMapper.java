package com.chicmic.trainingModule.Service.AssignTaskService;

import com.chicmic.trainingModule.Dto.AssignTaskDto.TaskDto;
import com.chicmic.trainingModule.Dto.CourseDto.CourseResponseDto;
import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.chicmic.trainingModule.Entity.Constants.EntityType;
import com.chicmic.trainingModule.Entity.Course;
import com.chicmic.trainingModule.Entity.PlanTask;
import com.chicmic.trainingModule.Entity.SubTask;
import com.chicmic.trainingModule.Entity.Task;
import com.chicmic.trainingModule.Repository.SubTaskRepo;
import com.chicmic.trainingModule.Service.UserProgressService.UserProgressService;
import com.chicmic.trainingModule.Service.UserTimeService.UserTimeService;
import com.chicmic.trainingModule.TrainingModuleApplication;
import com.chicmic.trainingModule.Util.FormatTime;
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
    private final UserTimeService userTimeService;


    public List<TaskDto> mapTaskToResponseDto(List<Task> taskList, String planId, String courseId, String traineeId, PlanTask planTask) {
        List<TaskDto> result = new ArrayList<TaskDto>();
        for (Task task : taskList) {
            for (SubTask subTask : task.getSubtasks()) {
                System.out.println("Subtask " + subTask);
                System.out.println("SubTaskId " + subTask.get_id());
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
                String reference = "";
                if (subTask.getEntityType() == EntityType.TEST) {
                    reference = subTask.getReference();
                } else {
                    reference = subTask.getLink();
                }
                Integer consumedTime = userTimeService.getTotalTimeByTraineeIdAndPlanIdAndPlanTaskIdAndSubTaskId(traineeId, planId, planTask.get_id(), subTask.get_id());
                TaskDto taskDto = TaskDto.builder()
                        .mainTask(mainTask)
                        .consumedTime(FormatTime.formatTimeIntoHHMM(consumedTime))
                        .estimatedTime(subTask.getEstimatedTime())
                        .reference(reference)
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
