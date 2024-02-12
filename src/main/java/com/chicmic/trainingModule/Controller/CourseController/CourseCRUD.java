package com.chicmic.trainingModule.Controller.CourseController;

import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponse;
import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponseWithCount;
import com.chicmic.trainingModule.Dto.CourseDto.CourseDto;
import com.chicmic.trainingModule.Dto.CourseDto.CourseResponseDto;
import com.chicmic.trainingModule.Entity.Course;

import com.chicmic.trainingModule.Entity.Phase;
import com.chicmic.trainingModule.Entity.PlanTask;
import com.chicmic.trainingModule.Entity.Task;
import com.chicmic.trainingModule.Repository.PlanTaskRepo;
import com.chicmic.trainingModule.Service.CourseServices.CourseResponseMapper;
import com.chicmic.trainingModule.Service.CourseServices.CourseService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;

//vector<int>1234
@RestController
@RequestMapping("/v1/training/course")
@AllArgsConstructor
@PreAuthorize("hasAnyAuthority('TL', 'PA', 'PM')")
public class CourseCRUD {
    private final CourseService courseService;
    private final PlanTaskRepo planTaskRepo;
    private final CourseResponseMapper courseResponseMapper;

    @RequestMapping(value = {""}, method = RequestMethod.GET)
    public ApiResponseWithCount getAll(
            @RequestParam(value = "index", defaultValue = "0", required = false) Integer pageNumber,
            @RequestParam(value = "limit", defaultValue = "10", required = false) Integer pageSize,
            @RequestParam(value = "searchString", defaultValue = "", required = false) String searchString,
            @RequestParam(value = "sortDirection", defaultValue = "1", required = false) Integer sortDirection,
            @RequestParam(value = "sortKey", defaultValue = "createdAt", required = false) String sortKey,
            @RequestParam(required = false) String courseId,
            @RequestParam(required = false, defaultValue = "false") Boolean isPhaseRequired,
            @RequestParam(required = false, defaultValue = "false") Boolean isDropdown,
            HttpServletResponse response,
            @RequestParam(required = false ) String traineeId,
            Principal principal
    )  {
        if(sortKey != null && sortKey.equals("createdAt")){
            sortDirection = -1;
        }
        if(sortKey != null && !sortKey.isEmpty() && sortKey.equals("courseName")){
            sortKey = "name";
        }
        System.out.println("dropdown key = " + isDropdown);
        if (isDropdown) {
            sortKey = "name";
            sortDirection = 1;
            List<Course> courseList = courseService.getAllCourses(searchString, sortDirection, sortKey, traineeId);
            Long count = courseService.countNonDeletedCourses(searchString, principal.getName());
            List<CourseResponseDto> courseResponseDtoList = courseResponseMapper.mapCourseToResponseDto(courseList, isPhaseRequired);
            return new ApiResponseWithCount(count, HttpStatus.OK.value(), courseResponseDtoList.size() + " Courses retrieved", courseResponseDtoList, response);
        }
        if(courseId == null || courseId.isEmpty()) {
            pageNumber /= pageSize;
            if (pageNumber < 0 || pageSize < 1)
                return new ApiResponseWithCount(0, HttpStatus.NO_CONTENT.value(), "invalid pageNumber or pageSize", null, response);
            List<Course> courseList = courseService.getAllCourses(pageNumber, pageSize, searchString, sortDirection, sortKey, principal.getName());
            Long count = courseService.countNonDeletedCourses(searchString, principal.getName());

            List<CourseResponseDto> courseResponseDtoList = courseResponseMapper.mapCourseToResponseDto(courseList, isPhaseRequired);
            return new ApiResponseWithCount(count, HttpStatus.OK.value(), courseResponseDtoList.size() + " Courses retrieved", courseResponseDtoList, response);
        } else {
            Course course = courseService.getCourseById(courseId);
            if(course == null){
                return new ApiResponseWithCount(0,HttpStatus.NOT_FOUND.value(), "Course not found", null, response);
            }
            CourseResponseDto courseResponseDto = courseResponseMapper.mapCourseToResponseDto(course, true);
            return new ApiResponseWithCount(1,HttpStatus.OK.value(), "Course retrieved successfully", courseResponseDto, response);
        }
    }


    @PostMapping
    public ApiResponse create(@RequestBody@Valid CourseDto courseDto, Principal principal) {
        System.out.println("\u001B[33m courseDto previos = " + courseDto);
        List<Phase<Task>> phases = new ArrayList<>();
//        for (List<Task> courseTasks : courseDto.getPhases()) {
//            Phase<Task> phase = Phase.<Task>builder()
//                    .entityType(EntityType.COURSE)
//                    .tasks(courseTasks)
//                    .build();
//            phases.add(phase);
//        }
        Course course = Course.builder()
                .createdBy(principal.getName())
                .name(courseDto.getName())
                .figmaLink(courseDto.getFigmaLink())
                .guidelines(courseDto.getGuidelines())
                .approver(courseDto.getApprover())
                .phases(courseDto.getPhases())
                .isDeleted(false)
                .isApproved(false)
                .build();
        System.out.println("course in controller  " + course);
        course = courseService.createCourse(course, false);
        return new ApiResponse(HttpStatus.CREATED.value(), "Course created successfully", course);
    }

    @DeleteMapping("/{courseId}")
    public ApiResponse delete(@PathVariable String courseId, HttpServletResponse response) {
        System.out.println("courseId = " + courseId);
        List<PlanTask> planTasks = planTaskRepo.findByPlanId(courseId);
        if(planTasks.size() > 0){
            return new ApiResponse(HttpStatus.BAD_REQUEST.value(), "Course is already assigned to a plan", null, response
            );
        }
        Boolean deleted = courseService.deleteCourseById(courseId);
        if (deleted) {
            return new ApiResponse(HttpStatus.OK.value(), "Course deleted successfully", null);
        }
        return new ApiResponse(HttpStatus.BAD_REQUEST.value(), "Course not found", null);
    }

    @PutMapping
    public ApiResponse updateCourse(@RequestBody@Valid CourseDto courseDto, @RequestParam String courseId, Principal principal, HttpServletResponse response) {
        Course course = courseService.getCourseById(courseId);
        System.out.println("course Dto = " + courseDto);
        if (courseDto.getApprover() != null && courseDto.getApprover().size() == 0) {
            return new ApiResponse(HttpStatus.BAD_REQUEST.value(), "Reviewers cannot be empty", null, response);
        }
        if (course != null) {
            if (courseDto != null && courseDto.getApproved() == true) {
                Set<String> approver = course.getApprover();
                if (approver.contains(principal.getName())) {
                    course = courseService.approve(course, principal.getName());
                } else {
                    return new ApiResponse(HttpStatus.BAD_REQUEST.value(), "You are not authorized to approve this course", null, response);

                }
            }
            courseDto.setApproved(course.getIsApproved());
            if (courseDto.getApprover() != null && !courseDto.getApprover().equals(course.getApprover())) {
                List<PlanTask> planTasks = planTaskRepo.findByPlanId(courseId);
                if(planTasks.size() > 0){
                    return new ApiResponse(HttpStatus.BAD_REQUEST.value(), "Reviewers cannot be edited since course is already assigned to a plan", null, response
                    );
                }
            }
//            courseDto.setApprover(course.getApprover());
            CourseResponseDto courseResponseDto = courseResponseMapper.mapCourseToResponseDto(courseService.updateCourse(courseDto, courseId), true);
            return new ApiResponse(HttpStatus.CREATED.value(), "Course updated successfully", courseResponseDto, response);
        }else {
            return new ApiResponse(HttpStatus.BAD_REQUEST.value(), "Course not found", null, response);
        }
    }
}