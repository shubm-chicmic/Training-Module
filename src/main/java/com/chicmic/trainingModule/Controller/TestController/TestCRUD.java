package com.chicmic.trainingModule.Controller.TestController;

import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponse;
import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponseWithCount;

import com.chicmic.trainingModule.Dto.TestDto.TestDto;
import com.chicmic.trainingModule.Dto.TestDto.TestResponseDto;
import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.chicmic.trainingModule.Entity.Milestone;
import com.chicmic.trainingModule.Entity.Task;
import com.chicmic.trainingModule.Entity.Test;
import com.chicmic.trainingModule.Entity.Phase;
import com.chicmic.trainingModule.Service.TestService.TestService;
import com.chicmic.trainingModule.Util.CustomObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/training/test")
@AllArgsConstructor
public class TestCRUD {
    private final TestService testService;
    private final RestTemplate restTemplate;

    @RequestMapping(value = {""}, method = RequestMethod.GET)
    public ApiResponseWithCount getAll(
            @RequestParam(value = "index", defaultValue = "0", required = false) Integer pageNumber,
            @RequestParam(value = "limit", defaultValue = "10", required = false) Integer pageSize,
            @RequestParam(value = "searchString", defaultValue = "", required = false) String searchString,
            @RequestParam(value = "sortDirection", defaultValue = "1", required = false) Integer sortDirection,
            @RequestParam(value = "sortKey", defaultValue = "", required = false) String sortKey,
            @RequestParam(required = false) String testId,
            HttpServletResponse response
    ) throws JsonProcessingException {
        if(testId == null || testId.isEmpty()) {
            pageNumber /= pageSize;
            if (pageNumber < 0 || pageSize < 1)
                return new ApiResponseWithCount(0, HttpStatus.NO_CONTENT.value(), "invalid pageNumber or pageSize", null, response);
            List<Test> testList = testService.getAllTests(pageNumber, pageSize, searchString, sortDirection, sortKey);
            Long count = testService.countNonDeletedTests();

            List<TestResponseDto> testResponseDtoList = CustomObjectMapper.mapTestToResponseDto(testList, false);
            Collections.reverse(testResponseDtoList);
            return new ApiResponseWithCount(count, HttpStatus.OK.value(), testResponseDtoList.size() + " Tests retrieved", testResponseDtoList, response);
        } else {
            Test test = testService.getTestById(testId);
            if(test == null){
                return new ApiResponseWithCount(0,HttpStatus.NOT_FOUND.value(), "Test not found", null, response);
            }
            TestResponseDto testResponseDto = CustomObjectMapper.mapTestToResponseDto(test);
            return new ApiResponseWithCount(1,HttpStatus.OK.value(), "Test retrieved successfully", testResponseDto, response);
        }
    }

    @PostMapping
    public ApiResponse create(@RequestBody TestDto testDto, Principal principal) {
        System.out.println("\u001B[33m testDto previos = " + testDto);
        List<Milestone> phases = new ArrayList<>();
//        for (List<Task> tasks : courseDto.getPhases()) {
//            Phase phase = Phase.builder()
//                    .tasks(tasks)
//                    .build();
//            phases.add(phase);
//        }
        Test test = Test.builder()
                .createdBy(principal.getName())
                .testName(testDto.getTestName())
                .teams(testDto.getTeams())
                .reviewers(testDto.getReviewers())
                .milestones(testDto.getMilestones())
                .status(testDto.getStatus())
                .deleted(false)
                .approved(false)
                .build();
        test = testService.createTest(test);
        return new ApiResponse(HttpStatus.CREATED.value(), "Test created successfully", test);
    }

    @DeleteMapping("/{testId}")
    public ApiResponse delete(@PathVariable String testId) {
        System.out.println("testId = " + testId);
        Boolean deleted = testService.deleteTestById(testId);
        if (deleted) {
            return new ApiResponse(HttpStatus.OK.value(), "Test deleted successfully", null);
        }
        return new ApiResponse(HttpStatus.NOT_FOUND.value(), "Test not found", null);
    }

    @PutMapping
    public ApiResponse updateTest(@RequestBody TestDto testDto, @RequestParam String testId, Principal principal, HttpServletResponse response) {
        Test test = testService.getTestById(testId);
        if (test != null) {
            if (testDto != null && testDto.getApproved() == true) {
                Set<String> approver = test.getReviewers();
                if (approver.contains(principal.getName())) {
                    test = testService.approve(test, principal.getName());
                } else {
                    return new ApiResponse(HttpStatus.FORBIDDEN.value(), "You are not authorized to approve this test", null, response);
                }
            }
            testDto.setApproved(test.getApproved());
            System.out.println("status = " + testDto.getStatus());
            if(testDto.getStatus() != null){
                if(testDto.getStatus() != 1 && testDto.getStatus() != 2 && testDto.getStatus() != 3) {
                    return new ApiResponse(HttpStatus.BAD_REQUEST.value(), "Status can only be 1 , 2 or 3", null, response);
                }
                if(!test.getApproved()) {
                    return new ApiResponse(HttpStatus.FORBIDDEN.value(), "You Can't update status since Test is not approved", null, response);
                }
                test = testService.updateStatus(testId, testDto.getStatus());
            }
            TestResponseDto testResponseDto = CustomObjectMapper.mapTestToResponseDto(testService.updateTest(testDto, testId));
            return new ApiResponse(HttpStatus.CREATED.value(), "Test updated successfully", testResponseDto, response);
        }else {
            return new ApiResponse(HttpStatus.NOT_FOUND.value(), "Test not found", null, response);
        }
    }
}
