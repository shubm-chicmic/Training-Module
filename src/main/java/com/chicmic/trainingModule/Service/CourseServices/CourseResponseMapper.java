package com.chicmic.trainingModule.Service.CourseServices;

import com.chicmic.trainingModule.Dto.CourseDto.CourseResponseDto;
import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.chicmic.trainingModule.Entity.Course;
import com.chicmic.trainingModule.TrainingModuleApplication;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CourseResponseMapper {
    public static List<CourseResponseDto> mapCourseToResponseDto(List<Course> courses, Boolean isPhaseRequired) {
        List<CourseResponseDto> courseResponseDtoList = new ArrayList<>();

        for (Course course : courses) {
            courseResponseDtoList.add(mapCourseToResponseDto(course, isPhaseRequired));
        }

        return courseResponseDtoList;
    }

//    public static String calculateTotalEstimatedTime(List<Phase> phases) {
//
//        long totalHours = 0;
//        long totalMinutes = 0;
//        for (Phase phase : phases) {
//            long phaseHours = 0;
//            long phaseMinutes = 0;
//
//            for (CourseTask courseTask : phase.getTasks()) {
//                for (CourseSubTask courseSubTask : courseTask.getSubtasks()) {
//
////                        System.out.println("Estimated time : " + subTask.getEstimatedTime());
//                    String[] timeParts = courseSubTask.getEstimatedTime().split(":");
//                    if (timeParts.length == 1) {
//                        phaseHours += (timeParts[0] != null && !timeParts[0].isEmpty()) ? Long.parseLong(timeParts[0]) : 0;
//                    } else if (timeParts.length == 2) {
//                        phaseHours += (timeParts[0] != null && !timeParts[0].isEmpty()) ? Long.parseLong(timeParts[0]) : 0;
//                        phaseMinutes += (timeParts[1] != null && !timeParts[1].isEmpty()) ? Long.parseLong(timeParts[1]) : 0;
//                    }
//                }
//            }
//
//            totalHours += phaseHours + phaseMinutes / 60;
//            totalMinutes += phaseMinutes % 60;
//
//        }
//        // Convert total hours and minutes to proper format
//        totalHours += totalMinutes / 60;
//        totalMinutes %= 60;
//
//        return String.format("%02d:%02d", totalHours, totalMinutes);
//    }


    public static CourseResponseDto mapCourseToResponseDto(Course course, Boolean isPhaseRequired) {


//        String totalEstimatedTime = calculateTotalEstimatedTime(course.getPhases());
//        int noOfTopics = 0;
//        for (Phase phase : course.getPhases()) {
//            int numOfTasksTopics = 0;
//            String estimatedTime = null;
//            for (CourseTask courseTask : phase.getTasks()) {
//                noOfTopics += courseTask.getSubtasks().size();
//                numOfTasksTopics += courseTask.getSubtasks().size();
//                long totalHours = 0;
//                long totalMinutes = 0;
//                long phaseHours = 0;
//                long phaseMinutes = 0;
//                for (CourseSubTask courseSubTask : courseTask.getSubtasks()) {
//                    String[] timeParts = courseSubTask.getEstimatedTime().split(":");
//                    if (timeParts.length == 1) {
//                        phaseHours += (timeParts[0] != null && !timeParts[0].isEmpty()) ? Long.parseLong(timeParts[0]) : 0;
//                    } else if (timeParts.length == 2) {
//                        phaseHours += (timeParts[0] != null && !timeParts[0].isEmpty()) ? Long.parseLong(timeParts[0]) : 0;
//                        phaseMinutes += (timeParts[1] != null && !timeParts[1].isEmpty()) ? Long.parseLong(timeParts[1]) : 0;
//                    }
//                }
//                totalHours += phaseHours + phaseMinutes / 60;
//                totalMinutes += phaseMinutes % 60;
//                estimatedTime = String.format("%02d:%02d", totalHours, totalMinutes);
//                System.out.println("Estimated time = " + estimatedTime);
//            }
//            phase.setNoOfTasks(numOfTasksTopics);
//            phase.setEstimatedTime(estimatedTime);
//        }
        return CourseResponseDto.builder()
                ._id(course.get_id())
                .guidelines(course.getGuidelines())
                .courseName(course.getName())
                .estimatedTime(course.getTotalEstimateTime())
                .noOfTopics(course.getTotalSubTasks())
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
