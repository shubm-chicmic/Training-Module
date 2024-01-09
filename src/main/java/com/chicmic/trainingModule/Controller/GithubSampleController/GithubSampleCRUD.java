package com.chicmic.trainingModule.Controller.GithubSampleController;


import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponse;
import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponseWithCount;
import com.chicmic.trainingModule.Dto.GithubSampleDto.GithubSampleDto;
import com.chicmic.trainingModule.Dto.GithubSampleDto.GithubSampleResponseDto;
import com.chicmic.trainingModule.Entity.GithubSample;
import com.chicmic.trainingModule.Service.GithubSampleServices.GithubSampleResponseMapper;
import com.chicmic.trainingModule.Service.GithubSampleServices.GithubSampleService;
import com.chicmic.trainingModule.Util.CustomObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;
//TODO approver dto update
@RestController
@RequestMapping("/v1/training/githubSample")
@AllArgsConstructor
public class GithubSampleCRUD {
    private final GithubSampleService githubSampleService;
    private final GithubSampleResponseMapper githubSampleResponseMapper;
    @RequestMapping(value = {""}, method = RequestMethod.GET)
    public ApiResponseWithCount getAll(
            @RequestParam(value = "index", defaultValue = "0", required = false) Integer pageNumber,
            @RequestParam(value = "limit", defaultValue = "10", required = false) Integer pageSize,
            @RequestParam(value = "searchString", defaultValue = "", required = false) String searchString,
            @RequestParam(value = "sortDirection", defaultValue = "1", required = false) Integer sortDirection,
            @RequestParam(value = "sortKey", defaultValue = "createdAt", required = false) String sortKey,
            @RequestParam(required = false) String githubSampleId,
            HttpServletResponse response,
            Principal principal
    ) throws JsonProcessingException {
        if(githubSampleId == null || githubSampleId.isEmpty()) {
            pageNumber /= pageSize;
            if (pageNumber < 0 || pageSize < 1)
                return new ApiResponseWithCount(0, HttpStatus.NO_CONTENT.value(), "invalid pageNumber or pageSize", null, response);
            List<GithubSample> githubSampleList = githubSampleService.getAllGithubSamples(pageNumber, pageSize, searchString, sortDirection, sortKey, principal.getName());
            Long count = githubSampleService.countNonDeletedGithubSamples(searchString, principal.getName());

            List<GithubSampleResponseDto> githubSampleResponseDtoList = githubSampleResponseMapper.mapGithubSampleToResponseDto(githubSampleList);
//            Collections.reverse(githubSampleResponseDtoList);
            return new ApiResponseWithCount(count, HttpStatus.OK.value(), githubSampleResponseDtoList.size() + " GithubSamples retrieved", githubSampleResponseDtoList, response);
        } else {
            System.out.println("i m called");
            GithubSample githubSample = githubSampleService.getGithubSampleById(githubSampleId);
            if(githubSample == null){
                return new ApiResponseWithCount(0,HttpStatus.NOT_FOUND.value(), "GithubSample not found", null, response);
            }
            GithubSampleResponseDto githubSampleResponseDto = githubSampleResponseMapper.mapGithubSampleToResponseDto(githubSample);
            return new ApiResponseWithCount(1,HttpStatus.OK.value(), "GithubSample retrieved successfully", githubSampleResponseDto, response);
        }
    }

    @PostMapping
    public ApiResponse create(@RequestBody GithubSampleDto githubSampleDto, Principal principal) {
        System.out.println("githubSampleDto = " + githubSampleDto);

        githubSampleDto.setCreatedBy(principal.getName());
        githubSampleDto = CustomObjectMapper.convert(githubSampleService.createGithubSample(CustomObjectMapper.convert(githubSampleDto, GithubSample.class)), GithubSampleDto.class);
        return new ApiResponse(HttpStatus.CREATED.value(), "GithubSample created successfully", githubSampleDto);
    }

    @DeleteMapping("/{githubSampleId}")
    public ApiResponse delete(@PathVariable String githubSampleId) {
        System.out.println("githubSampleId = " + githubSampleId);
        Boolean deleted = githubSampleService.deleteGithubSampleById(githubSampleId);
        if (deleted) {
            return new ApiResponse(HttpStatus.OK.value(), "GithubSample deleted successfully", null);
        }
        return new ApiResponse(HttpStatus.NOT_FOUND.value(), "GithubSample not found", null);
    }

    @PutMapping
    public ApiResponse updateGithubSample(@RequestBody GithubSampleDto githubSampleDto, @RequestParam String githubSampleId, Principal principal, HttpServletResponse response) {
        GithubSample githubSample = githubSampleService.getGithubSampleById(githubSampleId);
        if (githubSample != null) {
            if (githubSampleDto.getApproved()) {
                Set<String> approver = githubSample.getApprover();
                if (approver.contains(principal.getName())) {
                    githubSample = githubSampleService.approve(githubSample, principal.getName());
                } else {
                    return new ApiResponse(HttpStatus.FORBIDDEN.value(), "You are not authorized to approve this GithubSample", null, response);
                }
            }
            GithubSampleResponseDto githubSampleResponseDto = githubSampleResponseMapper.mapGithubSampleToResponseDto(githubSampleService.updateGithubSample(githubSampleDto, githubSampleId));
            return new ApiResponse(HttpStatus.CREATED.value(), "GithubSample updated successfully", githubSampleResponseDto, response);
        } else {
            return new ApiResponse(HttpStatus.NOT_FOUND.value(), "GithubSample not found", null, response);
        }
    }
}
