package com.chicmic.trainingModule.Service.CourseServices;

import com.chicmic.trainingModule.Dto.CourseDto.CourseResponseDto;
import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.chicmic.trainingModule.Entity.Course;
import com.chicmic.trainingModule.TrainingModuleApplication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CourseResponseMapper {
    public static List<CourseResponseDto> mapCourseToResponseDto(List<Course> courses, Boolean isPhaseRequired) {
        List<CourseResponseDto> courseResponseDtoList = new ArrayList<>();

        for (Course course : courses) {
            courseResponseDtoList.add(mapCourseToResponseDto(course, isPhaseRequired));
        }

        return courseResponseDtoList;
    }


    public static CourseResponseDto mapCourseToResponseDto(Course course, Boolean isPhaseRequired) {
        System.out.println("Size = " + course);
        return CourseResponseDto.builder()
                ._id(course.get_id())
                .guidelines(course.getGuidelines())
                .courseName(course.getName())
                .estimatedTime(course.getEstimatedTime())
                .noOfTopics(course.getTotalTasks())
                .figmaLink(course.getFigmaLink())
                .approver(course.getApproverDetails())
                .totalPhases(course.getPhases().size())
                .phases(course.getPhases())
                .deleted(course.getIsDeleted())
                .approvedBy(course.getApprovedByDetails())
                .approved(course.getIsApproved())
                .createdBy(course.getCreatedBy())
                .createdByName(TrainingModuleApplication.searchNameById(course.getCreatedBy()))
                .build();
    }

}
