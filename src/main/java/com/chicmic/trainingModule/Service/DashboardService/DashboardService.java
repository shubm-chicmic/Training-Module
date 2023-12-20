package com.chicmic.trainingModule.Service.DashboardService;

import com.chicmic.trainingModule.Controller.DashboardController.DashboardCRUD;
import com.chicmic.trainingModule.Dto.DashboardDto.CourseDto;
import com.chicmic.trainingModule.Dto.DashboardDto.DashboardResponse;
import com.chicmic.trainingModule.Dto.DashboardDto.PlanDto;
import com.chicmic.trainingModule.Dto.UserDto;
import com.chicmic.trainingModule.Service.FeedBackService.FeedbackService;
import com.chicmic.trainingModule.TrainingModuleApplication;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

@Service
public class DashboardService {

    private final FeedbackService feedbackService;

    public DashboardService( FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    public DashboardResponse getTraineeRatingSummary(String traineeId){
        //checking traineeId is valid or not
        UserDto userDto = TrainingModuleApplication.searchUserById(traineeId);
        DashboardResponse dashboardResponse = feedbackService.findFeedbacksSummaryOfTrainee(traineeId);
        dashboardResponse.setName(userDto.getName());
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        //fetch plans of user
        if(dashboardResponse.getFeedbacks().size()>0) {
            dashboardResponse.setCourses(Arrays.asList(CourseDto.builder().name("ReactJs").progress(50).build(),
                    CourseDto.builder().name("VueJS").progress(53).build(),
                    CourseDto.builder().name("NodeJs").progress(63).build()));
            dashboardResponse.setPlan(Arrays.asList(PlanDto.builder()
                            .name("Initial Plan")
                            .date(formatter.format(new Date()))
                            .phase("Planning")
                            .isComplete(true)
                            .build(),
                    PlanDto.builder()
                            .name("Implementation Plan")
                            .date(formatter.format(new Date()))
                            .phase("Implementation")
                            .isComplete(false)
                            .build()
            ));
        }
        return dashboardResponse;
    }
}
