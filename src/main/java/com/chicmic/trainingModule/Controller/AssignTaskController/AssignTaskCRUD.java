package com.chicmic.trainingModule.Controller.AssignTaskController;

import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponse;
import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponseWithCount;
import com.chicmic.trainingModule.Dto.AssignTaskDto.*;
import com.chicmic.trainingModule.Entity.*;
import com.chicmic.trainingModule.Service.AssignTaskService.AssignPlanResponseMapper;
import com.chicmic.trainingModule.Service.AssignTaskService.AssignTaskResponseMapper;
import com.chicmic.trainingModule.Service.AssignTaskService.AssignTaskService;
import com.chicmic.trainingModule.Service.CourseServices.CourseService;
import com.chicmic.trainingModule.Service.PlanServices.PlanService;
import com.chicmic.trainingModule.Service.PlanServices.PlanTaskService;
import com.chicmic.trainingModule.Service.TestServices.TestService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

@RestController
@RequestMapping("/v1/training/assignedPlan")
@AllArgsConstructor
public class AssignTaskCRUD {
    private final AssignTaskService assignTaskService;
    private final PlanService planService;
    private final CourseService courseService;
    private final TestService testService;
    private final PlanTaskService planTaskService;
    private final AssignTaskResponseMapper assignTaskResponseMapper;
    private final AssignPlanResponseMapper assignPlanResponseMapper;
//    private final TraineePlanService trainePlanService;
    @PostMapping
    public ApiResponse create(@RequestBody AssignTaskDto assignTaskDto, Principal principal, HttpServletResponse response) {
//        trainePlanService.assignMultiplePlansToTrainees();
//        PlanRequestDto planRequestDto = PlanRequestDto.builder().trainees(new HashSet<>( assignTaskDto.getUsers())).planId(assignTaskDto.getPlanIds().get(0))
//                .reviewers(assignTaskDto.getApprover()).build();
//        trainePlanService.assignMultiplePlansToTrainees(planRequestDto, principal.getName());

        System.out.println("assignTaskDto = " + assignTaskDto);
        Boolean error = false;
        for (String userId : assignTaskDto.getUsers()) {
            AssignedPlan assignTask = assignTaskService.createAssignTask(assignTaskDto, userId, principal);
            if(assignTask == null){
                error = true;
            }
        }
        if(error) {
            return new ApiResponse(HttpStatus.BAD_REQUEST.value(), "Plan is Already Assigned To Some Trainees", null, response);
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
            HttpServletResponse response,
            Principal principal
    )  {

        if (traineeId != null || !traineeId.isEmpty()){
            System.out.println("im in");
            AssignedPlan assignTaskList = assignTaskService.getAllAssignTasksByTraineeId(traineeId);
//            System.out.println(assignTaskList.size());
//            Long count = assignTaskService.countNonDeletedAssignTasksByTraineeId(traineeId);
            List<PlanDto> planDtoList = assignTaskResponseMapper.mapAssignTaskToResponseDto(assignTaskList, traineeId, principal);
//            Collections.reverse(assignTaskResponseDtoList);
            return new ApiResponseWithCount(1,HttpStatus.OK.value(), planDtoList.size() + " Plans retrieved", planDtoList, response);
        }
        return new ApiResponseWithCount(0,HttpStatus.BAD_REQUEST.value(), "Trainee Not Fount", null, response);
    }
    @GetMapping("/plan")
    public ApiResponseWithCount getPlan(@RequestParam String planId,
                               HttpServletResponse response
    ){
        Plan plan = planService.getPlanById(planId);
        if(plan != null) {
           List<Phase<PlanTask>> phases = plan.getPhases();
           List<PlanTask> planTasks = new ArrayList<>();
           for (Phase<PlanTask> phase : phases) {
               planTasks.addAll(phase.getTasks());
           }
           List<PlanTaskResponseDto> planTaskResponseDtoList = assignPlanResponseMapper.mapAssignPlanToResponseDto(planTasks);
           return new ApiResponseWithCount(0, HttpStatus.OK.value(), "Plan Retrieved", planTaskResponseDtoList, response);
        }
        return new ApiResponseWithCount(0, HttpStatus.BAD_REQUEST.value(), "Plan Not Found", null, response);

    }
    @GetMapping("/planTask")
    public ApiResponseWithCount getPlanTask(@RequestParam String planTaskId,
                                        HttpServletResponse response
    ){
        PlanTask planTask = planTaskService.getPlanTaskById(planTaskId);
        if(planTask != null) {
            List<String> phasesList = planTask.getPhases();
            List<TaskDto> taskDtoList = new ArrayList<>();
            List<Phase> phases = courseService.getPhaseByIds(phasesList);
            List<Task> taskList = new ArrayList<>();
            for(Phase phase : phases) {
                taskList.addAll(phase.getTasks());
            }
        }
        return null;
    }




}
