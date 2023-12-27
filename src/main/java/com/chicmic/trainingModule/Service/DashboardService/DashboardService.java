package com.chicmic.trainingModule.Service.DashboardService;

import com.chicmic.trainingModule.Dto.DashboardDto.CourseDto;
import com.chicmic.trainingModule.Dto.DashboardDto.DashboardResponse;
import com.chicmic.trainingModule.Dto.DashboardDto.PlanDto;
import com.chicmic.trainingModule.Dto.UserDto;
import com.chicmic.trainingModule.Entity.AssignTask.AssignTask;
import com.chicmic.trainingModule.Entity.AssignTask.AssignTaskPlanTrack;
import com.chicmic.trainingModule.Entity.Plan.Phase;
import com.chicmic.trainingModule.Entity.Plan.Plan;
import com.chicmic.trainingModule.Entity.Plan.Task;
import com.chicmic.trainingModule.Service.FeedBackService.FeedbackService;
import com.chicmic.trainingModule.TrainingModuleApplication;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final FeedbackService feedbackService;
    private final MongoTemplate mongoTemplate;

    public DashboardService(FeedbackService feedbackService, MongoTemplate mongoTemplate) {
        this.feedbackService = feedbackService;
        this.mongoTemplate = mongoTemplate;
    }

    public DashboardResponse getTraineeRatingSummary(String traineeId){
        //checking traineeId is valid or not
        UserDto userDto = TrainingModuleApplication.searchUserById(traineeId);
        DashboardResponse dashboardResponse = feedbackService.findFeedbacksSummaryOfTrainee(traineeId);
        dashboardResponse.setName(userDto.getName());
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        //fetch plans of user
//        if(dashboardResponse.getFeedbacks().size()>=0) {
//            dashboardResponse.setCourses(Arrays.asList(CourseDto.builder().name("ReactJs").progress(50).build(),
//                    CourseDto.builder().name("VueJS").progress(53).build(),
//                    CourseDto.builder().name("NodeJs").progress(63).build()));
//            dashboardResponse.setPlan(Arrays.asList(PlanDto.builder()
//                            .name("Initial Plan")
//                            .date(formatter.format(new Date()))
//                            .phase("Planning")
//                            .isComplete(true)
//                            .build(),
//                    PlanDto.builder()
//                            .name("Implementation Plan")
//                            .date(formatter.format(new Date()))
//                            .phase("Implementation")
//                            .isComplete(false)
//                            .build()
//            ));
//        }
        //get
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Criteria criteria = Criteria.where("userId").is(traineeId);
        Query query = new Query(criteria);
        AssignTask assignTask = mongoTemplate.findOne(query, AssignTask.class);
        Map<String,List<AssignTaskPlanTrack>> assignCourseMap  = new HashMap<>();
        Map<String,List<AssignTaskPlanTrack>> assignTestMap = new HashMap<>();
        List<PlanDto> planDtoList = new ArrayList<>();
        Date date  = new Date();
        if(assignTask != null) {
            List<Plan> plans = assignTask.getPlans();
            List<CourseDto> courseDtos = new ArrayList<>();
            List<String> courseIds = new ArrayList<>();
            List<String> testIds = new ArrayList<>();
            for (Plan plan : plans) {
                for (Phase phase : plan.getPhases()) {
                    for (Task task : phase.getTasks()) {
                        String _id = ((AssignTaskPlanTrack) task.getPlan()).get_id();
                        Boolean isCompleted = ((AssignTaskPlanTrack) task.getPlan()).getIsCompleted();
                        if(task.getPlanType() != 3)
                            planDtoList.add(PlanDto.builder().name(phase.getPhaseName()).date(formatter.format(date)).isComplete(isCompleted)
                                .phase(_id).type(task.getPlanType()).build());
                        //AssignTaskPlanTrack assignTaskPlanTrack = ((AssignTaskPlanTrack) task.getPlan());
                        List<AssignTaskPlanTrack> milestones =  (List<AssignTaskPlanTrack>) task.getMilestones();
                        if(task.getPlanType() == 2){
                            assignTestMap.put(_id,milestones);
                            testIds.add(_id);
                        }
                        if(task.getPlanType() != 1) continue;
                        int count = 0;
                        for (AssignTaskPlanTrack milestone : milestones) {
                            if (milestone.getIsCompleted() == true)
                                ++count;
                        }
                        courseIds.add(_id);
                        courseDtos.add(new CourseDto(_id, count*100/Integer.max(1,milestones.size())));
                        assignCourseMap.put(_id,milestones);
                        // courseId.put(_id,count/ milestones.size() * 100);
                    }
                }
            }
            Map<String, Document> courseDetails = feedbackService.getCourseNameAndPhaseName(courseIds);
            Map<String,Document> testDetails = feedbackService.getTestNameAndMilestoneName(testIds);
            for (CourseDto courseDto : courseDtos) {
                String _id = courseDto.getName();
                String tp = (String) courseDetails.get(_id).get("name");
                courseDto.setName(tp);
            }
            for(PlanDto planDto : planDtoList){
                String _id = planDto.getPhase();
//                int type = Integer.min(3.planDt)
                if(planDto.getType()==1||planDto.getType()==4){
                    String tp = (String) courseDetails.get(_id).get("name");
                    planDto.setPhase(tp);
                }else if(planDto.getType()==2){
                    String tp = (String) testDetails.get(_id).get("testName");
                    planDto.setPhase(tp);
                }
            }
            Collections.sort(planDtoList);
            dashboardResponse.setCourses(courseDtos);
            dashboardResponse.setPlan(planDtoList);
            //---set plan details!!!
        }
        return dashboardResponse;
    }
}
