package com.chicmic.trainingModule.Controller.CourseController;

import com.chicmic.trainingModule.Dto.*;
import com.chicmic.trainingModule.Entity.Course;
import com.chicmic.trainingModule.Service.CourseService.CourseServices;
import com.chicmic.trainingModule.Util.CustomObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/training/course") // Adjust the request mapping
@AllArgsConstructor
public class CourseCRUD {

    private final CourseServices courseServices;

    @GetMapping
    public ApiResponse getAll(
            @RequestParam(value = "index", defaultValue = "0", required = false) Integer pageNumber,
            @RequestParam(value = "limit", defaultValue = "10", required = false) Integer pageSize
            ) {
        pageNumber /= pageSize;
        if (pageNumber < 0 || pageSize < 1)
            return new ApiResponse(HttpStatus.NO_CONTENT.value(), "Invalid pageNumber or pageSize", null);
        List<Course> courseList = courseServices.getAllCourses(pageNumber, pageSize);
        return new ApiResponse(HttpStatus.OK.value(), "Courses retrieved", courseList);
    }

    @GetMapping("/{courseId}")
    public ApiResponse get(@PathVariable Long courseId) {
        Course course = courseServices.getCourseById(courseId);
        return new ApiResponse(HttpStatus.OK.value(), "Course retrieved successfully", course);
    }

    @PostMapping
    public ApiResponse create(@RequestBody CourseDto courseDto) {
        courseDto = CustomObjectMapper.convert(courseServices.createCourse(CustomObjectMapper.convert(courseDto, Course.class)), CourseDto.class);
        return new ApiResponse(HttpStatus.CREATED.value(), "Course created successfully", courseDto);
    }

    @DeleteMapping("/{courseId}")
    public ApiResponse delete(@PathVariable Long courseId) {
        Boolean deleted = courseServices.deleteCourseById(courseId);
        if (deleted) {
            return new ApiResponse(HttpStatus.OK.value(), "Course deleted successfully", null);
        }
        return new ApiResponse(HttpStatus.NOT_FOUND.value(), "Course not found", null);
    }


    @PutMapping
    public ApiResponse updateCourse(@RequestBody CourseDto courseDto, @RequestParam Long id) {
        Course updatedCourse = courseServices.updateCourse(courseDto, id);
        return new ApiResponse(HttpStatus.CREATED.value(), "Course updated successfully", updatedCourse);
    }
}
