package com.chicmic.trainingModule.Controller.AssignTaskController;

import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponse;
import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponseWithCount;
import com.chicmic.trainingModule.Dto.AssignTaskDto.*;
import com.chicmic.trainingModule.Entity.*;
import com.chicmic.trainingModule.Entity.Constants.TrainingStatus;
import com.chicmic.trainingModule.Service.AssignTaskService.AssignPlanResponseMapper;
import com.chicmic.trainingModule.Service.AssignTaskService.AssignTaskResponseMapper;
import com.chicmic.trainingModule.Service.AssignTaskService.AssignTaskService;
import com.chicmic.trainingModule.Service.AssignTaskService.TaskResponseMapper;
import com.chicmic.trainingModule.Service.CourseServices.CourseService;
import com.chicmic.trainingModule.Service.PhaseService;
import com.chicmic.trainingModule.Service.PlanServices.PlanService;
import com.chicmic.trainingModule.Service.PlanServices.PlanTaskService;
import com.chicmic.trainingModule.Service.TestServices.TestService;
import com.chicmic.trainingModule.Service.UserProgressService.UserProgressService;
import com.chicmic.trainingModule.Util.DateTimeUtil;
import com.chicmic.trainingModule.Util.Pagenation;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/training/assignedPlan")
@AllArgsConstructor
public class AssignTaskCRUD {
    private final AssignTaskService assignTaskService;
    private final PlanService planService;
    private final PhaseService phaseService;
    private final TestService testService;
    private final PlanTaskService planTaskService;
    private final AssignTaskResponseMapper assignTaskResponseMapper;
    private final AssignPlanResponseMapper assignPlanResponseMapper;
    private final TaskResponseMapper taskResponseMapper;
    private final UserProgressService userProgressService;

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
            AssignedPlan assignedPlan = assignTaskService.getAllAssignTasksByTraineeId(userId);
            if(assignedPlan == null || (assignedPlan != null && assignedPlan.getPlans().size() == 0)) {
                AssignedPlan assignTask = assignTaskService.createAssignTask(assignTaskDto, userId, principal);
            }else {
                List<Plan> plans = assignedPlan.getPlans();
                List<String> planIds = plans.stream()
                        .map(Plan::get_id)
                        .collect(Collectors.toList());
                int count = 0;
                for (String planDtoId : assignTaskDto.getPlanIds()){
                    if(!planIds.contains(planDtoId)){
                       Plan plan = planService.getPlanById(planDtoId);
                       plans.add(plan);
                       count++;
                    }
                }
                if(count != 0) {
                    assignedPlan.setUpdatedAt(LocalDateTime.now());
                    assignedPlan.setPlans(plans);
                    assignedPlan.setDate(assignTaskDto.getDate());
                    assignTaskService.updateAssignTask(assignedPlan);
                }
            }
        }
        if(error) {
            return new ApiResponse(HttpStatus.BAD_REQUEST.value(), "Plan Already Assigned", null, response);
        }
        return new ApiResponse(HttpStatus.CREATED.value(), "AssignTask Created Successfully", assignTaskDto);
    }
    @PutMapping
    public ApiResponse updateAssignTask(@RequestParam String userId, @RequestBody AssignedPlanUpdateDto assignTaskDto, HttpServletResponse response){
        AssignedPlan assignedPlan = assignTaskService.getAllAssignTasksByTraineeId(userId);
        if(assignedPlan != null) {
            List<Plan> plans = new ArrayList<>();
            for (String planId : assignTaskDto.getPlan()) {
                Plan plan = planService.getPlanById(planId);
                plans.add(plan);
            }

            if(plans != null || plans.size() != 0) {
                List<String> planIds = new ArrayList<>();
                for (Plan plan : assignedPlan.getPlans()) {
                    if(plan != null) {
                        planIds.add(plan.get_id());
                    }
                }
                for (String planDtoId : assignTaskDto.getPlan()){
                    if(!planIds.contains(planDtoId)){
                        //addedPlan
                        assignedPlan.setTrainingStatus(TrainingStatus.ONGOING);
                    }
                }
                for (String planId : planIds){
                    if(!assignTaskDto.getPlan().contains(planId)){
                        //deletedplan
                        assignedPlan.setTrainingStatus(TrainingStatus.ONGOING);
                        System.out.println("Plan Id " + planId);
                        userProgressService.deleteUserProgressByPlanId(userId,planId);
                    }
                }

                assignedPlan.setPlans(plans);
            }
            if(assignTaskDto.getDate() != null){
                assignedPlan.setDate(assignTaskDto.getDate());
            }
            AssignedPlan assignTask = assignTaskService.updateAssignTask(assignedPlan);
            return new ApiResponse(HttpStatus.OK.value(), "Assign Plan Updated successfully", assignTask, response);
        }
        return new ApiResponse(HttpStatus.BAD_REQUEST.value(), "Plan Not Assigned", null, response);
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
            AssignTaskResponseDto assignTaskResponseDto = assignTaskResponseMapper.mapAssignTaskToResponseDto(assignTaskList, traineeId, principal);
//            if(assignTaskResponseDto.getIsCompleted() == true){
//                System.out.println("creating copy of db");
//            }
            int totalPlans = 0;
            if(assignTaskResponseDto.getPlans() != null && assignTaskResponseDto.getPlans().size() != 0){
                List<PlanDto> plans = assignTaskResponseDto.getPlans();
                totalPlans = plans.size();
                plans = Pagenation.paginate(plans, pageNumber, pageSize);
                assignTaskResponseDto.setPlans(plans);
            }
//            Collections.reverse(assignTaskResponseDtoList);
            Integer planSize = 0;
            if(assignTaskResponseDto.getPlans() != null) {
                planSize = assignTaskResponseDto.getPlans().size();
            }
            return new ApiResponseWithCount(totalPlans,HttpStatus.OK.value(), planSize + " Plans retrieved", assignTaskResponseDto, response);
        }
        return new ApiResponseWithCount(0,HttpStatus.BAD_REQUEST.value(), "Trainee Not Found", null, response);
    }
    @GetMapping("/plan")
    public ApiResponseWithCount getPlan(@RequestParam String planId,
                               @RequestParam String traineeId,
                               @RequestParam(value = "index", defaultValue = "0", required = false) Integer pageNumber,
                               @RequestParam(value = "limit", defaultValue = "10", required = false) Integer pageSize,
                               HttpServletResponse response,
                                        Principal principal
    ){
        Plan plan = planService.getPlanById(planId);
        if(plan != null) {
           List<Phase<PlanTask>> phases = plan.getPhases();
           List<PlanTask> planTasks = new ArrayList<>();
           for (Phase<PlanTask> phase : phases) {
               if(phase != null) {
                   planTasks.addAll(phase.getTasks());
               }
           }
           int totalTasks = planTasks.size();
           planTasks = Pagenation.paginate(planTasks, pageNumber, pageSize);
            System.out.println("PlanTasks: " + planTasks.size());
           List<PlanTaskResponseDto> planTaskResponseDtoList = assignPlanResponseMapper.mapAssignPlanToResponseDto(planTasks, planId,traineeId, principal.getName());
           return new ApiResponseWithCount(totalTasks, HttpStatus.OK.value(), "Plan Retrieved", planTaskResponseDtoList, response);
        }
        return new ApiResponseWithCount(0, HttpStatus.BAD_REQUEST.value(), "Plan Not Found", null, response);

    }
    @GetMapping("/planTask")
    public ApiResponseWithCount getPlanTask(@RequestParam String planTaskId,
                                        @RequestParam String traineeId,
                                        @RequestParam String planId,
                                            @RequestParam(value = "index", defaultValue = "0", required = false) Integer pageNumber,
                                            @RequestParam(value = "limit", defaultValue = "10", required = false) Integer pageSize,
                                        HttpServletResponse response
    ){
        PlanTask planTask = planTaskService.getPlanTaskById(planTaskId);

        if(planTask != null) {
            String courseId = planTask.getPlan();
            List<Object> phasesList = planTask.getMilestones();
            List<TaskDto> taskDtoList = new ArrayList<>();
            List<String> phasesListOfString = new ArrayList<>();
            for (Object obj : phasesList) {
                phasesListOfString.add(obj.toString());
            }
            List<Phase> phases = phaseService.getPhaseByIds(phasesListOfString);
            List<Task> taskList = new ArrayList<>();
            for(Phase phase : phases) {
                if(!phase.getIsDeleted())
                taskList.addAll(phase.getTasks());
            }
            taskDtoList = taskResponseMapper.mapTaskToResponseDto(taskList,planId, courseId, traineeId);
            int totalTaskList = taskDtoList.size();
            taskDtoList = Pagenation.paginate(taskDtoList, pageNumber, pageSize);
            return new ApiResponseWithCount(totalTaskList, HttpStatus.OK.value(), "Plan Task Retrieved", taskDtoList, response);
        }
        return null;
    }




}
