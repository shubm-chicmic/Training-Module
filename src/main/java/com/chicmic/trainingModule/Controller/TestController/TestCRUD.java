package com.chicmic.trainingModule.Controller.TestController;

import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponse;
import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponseWithCount;
import com.chicmic.trainingModule.Dto.TestDto.TestDto;
import com.chicmic.trainingModule.Dto.TestDto.TestResponseDto;
import com.chicmic.trainingModule.Entity.Test.Milestone;
import com.chicmic.trainingModule.Entity.Test.Test;
import com.chicmic.trainingModule.Entity.Test.TestTask;
import com.chicmic.trainingModule.Service.TestServices.TestService;
import com.chicmic.trainingModule.Util.CustomObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/v1/training/test")
@AllArgsConstructor
public class TestCRUD {
    private final TestService testService;
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
            Principal principal
    )  {
        System.out.println("dropdown key = " + isDropdown);
        if (isDropdown) {
            List<Test> testList = testService.getAllTests(searchString, sortDirection, sortKey);
            Long count = testService.countNonDeletedTests(searchString);
            List<TestResponseDto> testResponseDtoList = CustomObjectMapper.mapTestToResponseDto(testList, isPhaseRequired);
            Collections.reverse(testResponseDtoList);
            return new ApiResponseWithCount(count, HttpStatus.OK.value(), testResponseDtoList.size() + " Tests retrieved", testResponseDtoList, response);
        }
        if(testId == null || testId.isEmpty()) {
            pageNumber /= pageSize;
            if (pageNumber < 0 || pageSize < 1)
                return new ApiResponseWithCount(0, HttpStatus.NO_CONTENT.value(), "invalid pageNumber or pageSize", null, response);
            List<Test> testList = testService.getAllTests(pageNumber, pageSize, searchString, sortDirection, sortKey, principal.getName());
            System.out.println(testList);
            Long count = testService.countNonDeletedTests(searchString);

            List<TestResponseDto> testResponseDtoList = CustomObjectMapper.mapTestToResponseDto(testList, isPhaseRequired);
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
        List<Milestone> milestones = new ArrayList<>();
        for (List<TestTask> tasks : testDto.getMilestones()) {
            Milestone milestone = Milestone.builder()
                    ._id(String.valueOf(new ObjectId()))
                    .tasks(tasks)
                    .build();
            milestones.add(milestone);
        }
        Test test = Test.builder()
                .createdBy(principal.getName())
                .testName(testDto.getTestName())
                .reviewers(testDto.getReviewers())
                .teams(testDto.getTeams())
                .createdBy(principal.getName())
                .milestones(milestones)
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
        if (testDto.getReviewers() != null && testDto.getReviewers().size() == 0) {
            return new ApiResponse(HttpStatus.BAD_REQUEST.value(), "Reviewers cannot be empty", null, response);
        }
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
            TestResponseDto testResponseDto = CustomObjectMapper.mapTestToResponseDto(testService.updateTest(testDto, testId));
            return new ApiResponse(HttpStatus.CREATED.value(), "Test updated successfully", testResponseDto, response);
        }else {
            return new ApiResponse(HttpStatus.NOT_FOUND.value(), "Test not found", null, response);
        }
    }
}
