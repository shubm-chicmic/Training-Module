package com.chicmic.trainingModule.Controller.AssignTaskController;

import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponse;
import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponseWithCount;
import com.chicmic.trainingModule.Dto.AssignTaskDto.AssignTaskDto;
import com.chicmic.trainingModule.Dto.AssignTaskDto.AssignTaskResponseDto;
import com.chicmic.trainingModule.Dto.AssignTaskDto.TaskCompleteDto;
import com.chicmic.trainingModule.Dto.CourseDto.CourseDto;
import com.chicmic.trainingModule.Entity.AssignTask.AssignTask;
import com.chicmic.trainingModule.Service.AssignTaskService.AssignTaskService;
import com.chicmic.trainingModule.Service.CourseServices.CourseService;
import com.chicmic.trainingModule.Util.CustomObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/v1/training/assignTask")
@AllArgsConstructor
public class AssignTaskCRUD {
    private final AssignTaskService assignTaskService;
    private final CustomObjectMapper customObjectMapper;
    @PostMapping
    public ApiResponse create(@RequestBody AssignTaskDto assignTaskDto, Principal principal) {
        System.out.println("assignTaskDto = " + assignTaskDto);
        for (String userId : assignTaskDto.getUsers()) {
            AssignTask assignTask = assignTaskService.createAssignTask(assignTaskDto, userId, principal);
        }
        return new ApiResponse(HttpStatus.CREATED.value(), "AssignTask created successfully", assignTaskDto);
    }
    @RequestMapping(value = {""}, method = RequestMethod.GET)
    public ApiResponseWithCount getAll(
            @RequestParam(value = "index", defaultValue = "0", required = false) Integer pageNumber,
            @RequestParam(value = "limit", defaultValue = "10", required = false) Integer pageSize,
            @RequestParam(value = "searchString", defaultValue = "", required = false) String searchString,
            @RequestParam(value = "sortDirection", defaultValue = "1", required = false) Integer sortDirection,
            @RequestParam(value = "sortKey", defaultValue = "", required = false) String sortKey,
            @RequestParam(required = false) String assignTaskId,
            @RequestParam(required = false) String traineeId,
            @RequestParam(required = false, defaultValue = "false") Boolean isPhaseRequired,
            @RequestParam(required = false, defaultValue = "false") Boolean isDropdown,
            HttpServletResponse response
    )  {
        System.out.println("dropdown key = " + isDropdown);
        if (isDropdown) {
            List<AssignTask> assignTaskList = assignTaskService.getAllAssignTasks(searchString, sortDirection, sortKey);
            Long count = assignTaskService.countNonDeletedAssignTasks();
            List<AssignTaskResponseDto> assignTaskResponseDtoList = customObjectMapper.mapAssignTaskToResponseDto(assignTaskList, traineeId);
            Collections.reverse(assignTaskResponseDtoList);
            return new ApiResponseWithCount(count, HttpStatus.OK.value(), assignTaskResponseDtoList.size() + " AssignTasks retrieved", assignTaskResponseDtoList, response);
        }if (traineeId != null || !traineeId.isEmpty()){
            System.out.println("im in");
            AssignTask assignTaskList = assignTaskService.getAllAssignTasksByTraineeId(traineeId);
//            System.out.println(assignTaskList.size());
//            Long count = assignTaskService.countNonDeletedAssignTasksByTraineeId(traineeId);
            AssignTaskResponseDto assignTaskResponseDtoList = customObjectMapper.mapAssignTaskToResponseDto(assignTaskList, traineeId);
//            Collections.reverse(assignTaskResponseDtoList);
            return new ApiResponseWithCount(1,HttpStatus.OK.value(), assignTaskResponseDtoList + " AssignTasks retrieved", assignTaskResponseDtoList, response);
        }
        if(assignTaskId == null || assignTaskId.isEmpty()) {
            pageNumber /= pageSize;
            if (pageNumber < 0 || pageSize < 1)
                return new ApiResponseWithCount(0, HttpStatus.NO_CONTENT.value(), "invalid pageNumber or pageSize", traineeId, response);
            List<AssignTask> assignTaskList = assignTaskService.getAllAssignTasks(pageNumber, pageSize, searchString, sortDirection, sortKey);
            Long count = assignTaskService.countNonDeletedAssignTasks();

            List<AssignTaskResponseDto> assignTaskResponseDtoList = customObjectMapper.mapAssignTaskToResponseDto(assignTaskList, traineeId);
            Collections.reverse(assignTaskResponseDtoList);
            return new ApiResponseWithCount(count, HttpStatus.OK.value(), assignTaskResponseDtoList.size() + " AssignTasks retrieved", assignTaskResponseDtoList, response);
        }
        else {
            AssignTask assignTask = assignTaskService.getAssignTaskById(assignTaskId);
            if(assignTask == null){
                return new ApiResponseWithCount(0,HttpStatus.NOT_FOUND.value(), "AssignTask not found", null, response);
            }
            AssignTaskResponseDto assignTaskResponseDto = customObjectMapper.mapAssignTaskToResponseDto(assignTask, null);
            return new ApiResponseWithCount(1,HttpStatus.OK.value(), "AssignTask retrieved successfully", assignTaskResponseDto, response);
        }
    }

    @PostMapping("/complete")
    public ApiResponse completeTask(@RequestBody TaskCompleteDto taskCompleteDto, Principal principal) {
        AssignTask assignTask = assignTaskService.completeTask(taskCompleteDto, principal);
        return new ApiResponse(HttpStatus.CREATED.value(), "Task completed successfully", assignTask);
    }


}
