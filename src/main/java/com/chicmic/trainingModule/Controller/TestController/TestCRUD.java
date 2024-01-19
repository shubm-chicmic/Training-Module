package com.chicmic.trainingModule.Controller.TestController;

import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponse;
import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponseWithCount;
import com.chicmic.trainingModule.Dto.TestDto.TestDto;
import com.chicmic.trainingModule.Dto.TestDto.TestResponseDto;
import com.chicmic.trainingModule.Entity.Constants.EntityType;
import com.chicmic.trainingModule.Entity.Phase;
import com.chicmic.trainingModule.Entity.PlanTask;
import com.chicmic.trainingModule.Entity.Task;
import com.chicmic.trainingModule.Entity.Test;
import com.chicmic.trainingModule.Repository.PlanTaskRepo;
import com.chicmic.trainingModule.Service.TestServices.TestResponseMapper;
import com.chicmic.trainingModule.Service.TestServices.TestService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/v1/training/test")
@AllArgsConstructor
public class TestCRUD {
    private final TestService testService;
    private final PlanTaskRepo planTaskRepo;

    private final TestResponseMapper testResponseMapper;
    @RequestMapping(value = {""}, method = RequestMethod.GET)
    public ApiResponseWithCount getAll(
            @RequestParam(value = "index", defaultValue = "0", required = false) Integer pageNumber,
            @RequestParam(value = "limit", defaultValue = "10", required = false) Integer pageSize,
            @RequestParam(value = "searchString", defaultValue = "", required = false) String searchString,
            @RequestParam(value = "sortDirection", defaultValue = "1", required = false) Integer sortDirection,
            @RequestParam(value = "sortKey", defaultValue = "createdAt", required = false) String sortKey,
            @RequestParam(required = false) String testId,
            @RequestParam(required = false, defaultValue = "false") Boolean isPhaseRequired,
            @RequestParam(required = false, defaultValue = "false") Boolean isDropdown,
            HttpServletResponse response,
            @RequestParam(required = false) String taineeId,
            Principal principal
    )  {
        if(sortKey != null && sortKey.equals("createdAt")){
            sortDirection = -1;
        }
        System.out.println("dropdown key = " + isDropdown);
        if (isDropdown) {
            sortKey = "testName";
            sortDirection = 1;
            List<Test> testList = testService.getAllTests(searchString, sortDirection, sortKey, taineeId);
            Long count = testService.countNonDeletedTests(searchString, principal.getName());
            List<TestResponseDto> testResponseDtoList = testResponseMapper.mapTestToResponseDto(testList, isPhaseRequired);
//            Collections.reverse(testResponseDtoList);
            return new ApiResponseWithCount(count, HttpStatus.OK.value(), testResponseDtoList.size() + " Tests retrieved", testResponseDtoList, response);
        }
        if(testId == null || testId.isEmpty()) {
            pageNumber /= pageSize;
            if (pageNumber < 0 || pageSize < 1)
                return new ApiResponseWithCount(0, HttpStatus.BAD_REQUEST.value(), "invalid pageNumber or pageSize", null, response);
            List<Test> testList = testService.getAllTests(pageNumber, pageSize, searchString, sortDirection, sortKey, principal.getName());
            System.out.println(testList);
            Long count = testService.countNonDeletedTests(searchString, principal.getName());

            List<TestResponseDto> testResponseDtoList = testResponseMapper.mapTestToResponseDto(testList, isPhaseRequired);
//            Collections.reverse(testResponseDtoList);
            return new ApiResponseWithCount(count, HttpStatus.OK.value(), testResponseDtoList.size() + " Tests retrieved", testResponseDtoList, response);
        } else {
            Test test = testService.getTestById(testId);
            if(test == null){
                return new ApiResponseWithCount(0,HttpStatus.BAD_REQUEST.value(), "Test not found", null, response);
            }
            TestResponseDto testResponseDto = testResponseMapper.mapTestToResponseDto(test);
            return new ApiResponseWithCount(1,HttpStatus.OK.value(), "Test retrieved successfully", testResponseDto, response);
        }
    }

    @PostMapping
    public ApiResponse create(@RequestBody TestDto testDto, Principal principal) {
        System.out.println("\u001B[33m testDto previos = " + testDto);

        Test test = Test.builder()
                .createdBy(principal.getName())
                .testName(testDto.getTestName())
                .approver(testDto.getApprover())
                .teams(testDto.getTeams())
                .createdBy(principal.getName())
                .milestones(testDto.getMilestones())
                .deleted(false)
                .approved(false)
                .build();
        test = testService.createTest(test);
        return new ApiResponse(HttpStatus.OK.value(), "Test created successfully", test);
    }

    @DeleteMapping("/{testId}")
    public ApiResponse delete(@PathVariable String testId, HttpServletResponse response) {
        System.out.println("testId = " + testId);
        List<PlanTask> planTasks = planTaskRepo.findByPlanId(testId);
        if(planTasks.size() > 0){
            return new ApiResponse(HttpStatus.BAD_REQUEST.value(), "Test is already assigned to a plan", null, response
            );
        }
        Boolean deleted = testService.deleteTestById(testId);
        if (deleted) {
            return new ApiResponse(HttpStatus.OK.value(), "Test deleted successfully", null);
        }
        return new ApiResponse(HttpStatus.BAD_REQUEST.value(), "Test not found", null);
    }

    @PutMapping
    public ApiResponse updateTest(@RequestBody TestDto testDto, @RequestParam String testId, Principal principal, HttpServletResponse response) {
        Test test = testService.getTestById(testId);
        System.out.println("testDto = " + testDto.getApprover());
//        if (testDto.getReviewers() != null && testDto.getReviewers().size() == 0) {
//            return new ApiResponse(HttpStatus.BAD_REQUEST.value(), "Reviewers cannot be empty", null, response);
//        }
        if (test != null) {
            if (testDto != null && testDto.getApproved() == true) {
                Set<String> approver = test.getApprover();
                if (approver.contains(principal.getName())) {
                    test = testService.approve(test, principal.getName());
                    TestResponseDto testResponseDto = testResponseMapper.mapTestToResponseDto(test);
                    return new ApiResponse(HttpStatus.OK.value(), "Test approved successfully", testResponseDto, response);

                } else {
                    return new ApiResponse(HttpStatus.BAD_REQUEST.value(), "You are not authorized to approve this test", null, response);
                }
            }
            testDto.setApproved(test.getApproved());
            if (test.getApprover() != null && !testDto.getApprover().equals(test.getApprover())) {
                List<PlanTask> planTasks = planTaskRepo.findByPlanId(testId);
                if(planTasks.size() > 0){
                    return new ApiResponse(HttpStatus.BAD_REQUEST.value(), "Reviewers cannot be edited since Test is already assigned to a plan", null, response
                    );
                }
            }
            TestResponseDto testResponseDto = testResponseMapper.mapTestToResponseDto(testService.updateTest(testDto, testId));
            return new ApiResponse(HttpStatus.OK.value(), "Test updated successfully", testResponseDto, response);
        }else {
            return new ApiResponse(HttpStatus.BAD_REQUEST.value(), "Test not found", null, response);
        }
    }
}
