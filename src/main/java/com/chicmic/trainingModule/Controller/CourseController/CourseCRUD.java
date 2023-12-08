package com.chicmic.trainingModule.Controller.CourseController;

import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponse;
import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponseWithCount;
import com.chicmic.trainingModule.Dto.CourseDto.CourseDto;
import com.chicmic.trainingModule.Dto.CourseDto.CourseResponseDto;
import com.chicmic.trainingModule.Dto.SessionDto.Mommessage;

import com.chicmic.trainingModule.Entity.Course;
import com.chicmic.trainingModule.Service.CourseServices.CourseService;
import com.chicmic.trainingModule.Util.CustomObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/v1/training/course")
@AllArgsConstructor
public class CourseCRUD {
    private final CourseService courseService;
    private final RestTemplate restTemplate;

    @RequestMapping(value = {""}, method = RequestMethod.GET)
    public ApiResponseWithCount getAll(
            @RequestParam(value = "index", defaultValue = "0", required = false) Integer pageNumber,
            @RequestParam(value = "limit", defaultValue = "10", required = false) Integer pageSize,
            @RequestParam(value = "searchString", defaultValue = "", required = false) String searchString,
            @RequestParam(value = "sortDirection", defaultValue = "1", required = false) Integer sortDirection,
            @RequestParam(value = "sortKey", defaultValue = "", required = false) String sortKey,
            @RequestParam(required = false) String courseId,
            HttpServletResponse response
    ) throws JsonProcessingException {
        if(courseId == null || courseId.isEmpty()) {
            pageNumber /= pageSize;
            if (pageNumber < 0 || pageSize < 1)
                return new ApiResponseWithCount(0, HttpStatus.NO_CONTENT.value(), "invalid pageNumber or pageSize", null, response);
            List<Course> courseList = courseService.getAllCourses(pageNumber, pageSize, searchString, sortDirection, sortKey);
            Long count = courseService.countNonDeletedCourses();

            List<CourseResponseDto> courseResponseDtoList = CustomObjectMapper.mapCourseToResponseDto(courseList);
            Collections.reverse(courseResponseDtoList);
            return new ApiResponseWithCount(count, HttpStatus.OK.value(), courseResponseDtoList.size() + " Courses retrieved", courseResponseDtoList, response);
        } else {
            Course course = courseService.getCourseById(courseId);
            if(course == null){
                return new ApiResponseWithCount(0,HttpStatus.NOT_FOUND.value(), "Course not found", null, response);
            }
            CourseResponseDto courseResponseDto = CustomObjectMapper.mapCourseToResponseDto(course);
            return new ApiResponseWithCount(1,HttpStatus.OK.value(), "Course retrieved successfully", courseResponseDto, response);
        }
    }

    @PostMapping
    public ApiResponse create(@RequestBody CourseDto courseDto, Principal principal) {
        System.out.println("courseDto = " + courseDto);
        courseDto.setCreatedBy(principal.getName());
        courseDto = CustomObjectMapper.convert(courseService.createCourse(CustomObjectMapper.convert(courseDto, Course.class)), CourseDto.class);
        return new ApiResponse(HttpStatus.CREATED.value(), "Course created successfully", courseDto);
    }

    @DeleteMapping("/{courseId}")
    public ApiResponse delete(@PathVariable String courseId) {
        System.out.println("courseId = " + courseId);
        Boolean deleted = courseService.deleteCourseById(courseId);
        if (deleted) {
            return new ApiResponse(HttpStatus.OK.value(), "Course deleted successfully", null);
        }
        return new ApiResponse(HttpStatus.NOT_FOUND.value(), "Course not found", null);
    }

    @PutMapping
    public ApiResponse updateCourse(@RequestBody CourseDto courseDto, @RequestParam String courseId, Principal principal, HttpServletResponse response) {
        Course course = courseService.getCourseById(courseId);
        if (course != null) {
            if (courseDto != null && courseDto.getApproved() != null) {
                List<String> approver = course.getReviewers();
                if (approver.contains(principal.getName())) {
                    course =courseService.approve(course, principal.getName(), courseDto.getApproved());
                } else {
                    return new ApiResponse(HttpStatus.FORBIDDEN.value(), "You are not authorized to approve this course", null, response);

                }
            }
            courseDto.setApproved(course.getIsApproved());
            System.out.println("status = " + courseDto.getStatus());
            if(courseDto.getStatus() != null){
                if(!course.getIsApproved()) {
                    return new ApiResponse(HttpStatus.FORBIDDEN.value(), "You Can't update status since Course is not approved", null, response);
                }
                course = courseService.updateStatus(courseId, courseDto.getStatus());
            }
            CourseResponseDto courseResponseDto = CustomObjectMapper.mapCourseToResponseDto(courseService.updateCourse(courseDto, courseId));
            return new ApiResponse(HttpStatus.CREATED.value(), "Course updated successfully", courseResponseDto, response);
        }else {
            return new ApiResponse(HttpStatus.NOT_FOUND.value(), "Course not found", null, response);
        }
    }
}