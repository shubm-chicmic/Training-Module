package com.chicmic.trainingModule.Service.DashboardService;

import com.chicmic.trainingModule.Controller.DashboardController.DashboardCRUD;
import com.chicmic.trainingModule.Dto.DashboardDto.CourseDto;
import com.chicmic.trainingModule.Dto.DashboardDto.DashboardResponse;
import com.chicmic.trainingModule.Dto.DashboardDto.PlanDto;
import com.chicmic.trainingModule.Dto.UserDto;
import com.chicmic.trainingModule.Service.FeedBackService.FeedbackService;
import com.chicmic.trainingModule.TrainingModuleApplication;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import static com.mongodb.client.model.Aggregates.group;
import static com.mongodb.client.model.Aggregates.unwind;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;
import static org.springframework.data.mongodb.core.query.Criteria.where;

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
        //computing course detail!!
//        Aggregation aggregation = newAggregation(
//                match(
//                        where("userId").is("64e2e91decc13d506c72c267")
//                                .and("plans.phases.tasks.planType").is(1)
//                ),
//                unwind("$plans"),
//                unwind("$plans.phases"),
//                unwind("$plans.phases.tasks"),
//                group("$plans.phases.tasks.plan._id")
//                        .count().as("totalCount")
//                        .sum(
//                                cond("$plans.phases.tasks.milestones.isCompleted").then(1).otherwise(0)
//                        ).as("completedCount"),
//                project("_id", "totalCount", "completedCount")
//        );
        return dashboardResponse;
    }
}
