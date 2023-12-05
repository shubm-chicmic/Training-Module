package com.chicmic.trainingModule.Controller.GithubSampleController;

import com.chicmic.trainingModule.Dto.ApiResponse;
import com.chicmic.trainingModule.Dto.GithubSampleDto;
import com.chicmic.trainingModule.Entity.GithubSample;
import com.chicmic.trainingModule.Service.GitSampleServices.GithubSampleService;
import com.chicmic.trainingModule.Util.CustomObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/training/githubsample")
@AllArgsConstructor
public class GithubSampleCRUD {
    private final GithubSampleService githubSampleService;

    @GetMapping
    public ApiResponse getAll(
            @RequestParam(value = "index", defaultValue = "0", required = false) Integer pageNumber,
            @RequestParam(value = "limit", defaultValue = "10", required = false) Integer pageSize
    ) {
        pageNumber /= pageSize;
        if (pageNumber < 0 || pageSize < 1)
            return new ApiResponse(HttpStatus.NO_CONTENT.value(), "invalid pageNumber or pageSize", null);
        List<GithubSample> githubSampleList = githubSampleService.getAllGithubSamples(pageNumber, pageSize);
        return new ApiResponse(HttpStatus.OK.value(), "Sessions retrieved", githubSampleList);
    }

    @GetMapping("/{githubSampleId}")
    public ApiResponse get(@PathVariable Long githubSampleId) {
        GithubSample githubSample = githubSampleService.getGithubSampleById(githubSampleId);
        return new ApiResponse(HttpStatus.OK.value(), "Session retrieved successfully", githubSample);
    }

    @PostMapping
    public ApiResponse create(@RequestBody GithubSampleDto githubSampleDto) {
        githubSampleDto = CustomObjectMapper.convert(githubSampleService.createGithubSample(CustomObjectMapper.convert(githubSampleDto, GithubSample.class)), GithubSampleDto.class);
        return new ApiResponse(HttpStatus.CREATED.value(), "GithubSample created successfully", githubSampleDto);
    }

    @DeleteMapping("/{githubSampleId}")
    public ApiResponse delete(@PathVariable Long githubSampleId) {
        Boolean deleted = githubSampleService.deleteGithubSampleById(githubSampleId);
        if (deleted) {
            return new ApiResponse(HttpStatus.OK.value(), "GithubSample deleted successfully", null);
        }
        return new ApiResponse(HttpStatus.NOT_FOUND.value(), "GithubSample not found", null);
    }

    @PutMapping
    public ApiResponse updateGithubSample(@RequestBody GithubSampleDto githubSampleDto, @RequestParam Long githubSampleId) {
        GithubSampleDto updatedGithubSample = CustomObjectMapper.convert(githubSampleService.updateGithubSample(githubSampleDto, githubSampleId), GithubSampleDto.class);
        return new ApiResponse(HttpStatus.CREATED.value(), "GithubSample updated successfully", updatedGithubSample);
    }
}