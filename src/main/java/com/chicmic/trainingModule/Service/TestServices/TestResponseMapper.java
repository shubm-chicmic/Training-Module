package com.chicmic.trainingModule.Service.TestServices;

import com.chicmic.trainingModule.Dto.TestDto.TestResponseDto;
import com.chicmic.trainingModule.Entity.Test;
import com.chicmic.trainingModule.TrainingModuleApplication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
@Service
public class TestResponseMapper {
//    public static String calculateTotalEstimatedTimeInTest(List<Milestone> milestones) {
//
//        long totalHours = 0;
//        long totalMinutes = 0;
//        for (Milestone milestone : milestones) {
//            long phaseHours = 0;
//            long phaseMinutes = 0;
//
//            for (TestTask testTask : milestone.getTasks()) {
//                for (TestSubTask testSubTask : testTask.getSubtasks()) {
//
////                        System.out.println("Estimated time : " + subTask.getEstimatedTime());
//                    String[] timeParts = testSubTask.getEstimatedTime().split(":");
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

    public static List<TestResponseDto> mapTestToResponseDto(List<Test> tests, Boolean isMilestoneRequired) {
        List<TestResponseDto> testResponseDtoList = new ArrayList<>();
        for (Test test : tests) {
            testResponseDtoList.add(mapTestToResponseDto(test));
        }
        return testResponseDtoList;
    }

    public static TestResponseDto mapTestToResponseDto(Test test) {

        return TestResponseDto.builder()
                ._id(test.get_id())
                .testName(test.getTestName())
                .estimatedTime(test.getEstimatedTime())
                .noOfMilestones(test.getMilestones().size())
                .noOfTopics(test.getTotalTasks())
                .teams(test.getTeamsDetails())
                .approver(test.getApproverDetails())
                .milestones(test.getMilestones())
                .deleted(test.getDeleted())
                .approvedBy(test.getApprovedByDetails())
                .approved(test.getApproved())
                .createdBy(test.getCreatedBy())
                .createdByName(TrainingModuleApplication.searchNameById(test.getCreatedBy()))
                .build();
    }

}
