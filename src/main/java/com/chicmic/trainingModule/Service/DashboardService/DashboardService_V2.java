package com.chicmic.trainingModule.Service.DashboardService;

import com.chicmic.trainingModule.Dto.DashboardDto.CourseDto;
import com.chicmic.trainingModule.Dto.DashboardDto.DashboardResponse;
import com.chicmic.trainingModule.Dto.DashboardDto.PlanDto;
import com.chicmic.trainingModule.Dto.UserDto;
import com.chicmic.trainingModule.Entity.AssignedPlan;
import com.chicmic.trainingModule.Entity.Phase;
import com.chicmic.trainingModule.Entity.Plan;
import com.chicmic.trainingModule.Entity.PlanTask;
import com.chicmic.trainingModule.Service.FeedBackService.FeedbackService_V2;
import com.chicmic.trainingModule.TrainingModuleApplication;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class DashboardService_V2 {
    private final FeedbackService_V2 feedbackService;
    private final MongoTemplate mongoTemplate;

    public DashboardService_V2(FeedbackService_V2 feedbackService, MongoTemplate mongoTemplate) {
        this.feedbackService = feedbackService;
        this.mongoTemplate = mongoTemplate;
    }

    public DashboardResponse getTraineeRatingSummary(String traineeId){
        //checking traineeId is valid or not
        UserDto userDto = TrainingModuleApplication.searchUserById(traineeId);
        DashboardResponse dashboardResponse = feedbackService.findFeedbacksSummaryOfTrainee(traineeId);
        dashboardResponse.setName(userDto.getName());
        //get
        Criteria criteria = Criteria.where("userId").is(traineeId);
        Query query = new Query(criteria);

        AssignedPlan assignedPlan = mongoTemplate.findOne(query, AssignedPlan.class);
        if (assignedPlan == null) return dashboardResponse;

        var plans =  assignedPlan.getPlans();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        //AtomicReference<String> planId = new AtomicReference<>("");
        List<CourseDto> courseDtoList = new ArrayList<>();
        List<PlanDto> planDtoList = new ArrayList<>();
        plans.forEach((p)->{
            p.getPhases().forEach(ps -> {
                if (ps.getEntityType() == 1) courseDtoList.add(new CourseDto(ps.getName(),50));
                planDtoList.add(PlanDto.builder().name(p.getPlanName()).phase(ps.getName())
                        .isComplete(false).date(formatter.format(new Date())).build());
                    //planId.set(p.get_id());
            });
        });
        dashboardResponse.setCourses(courseDtoList);
        dashboardResponse.setPlan(planDtoList);
//        Map<String,List<AssignTaskPlanTrack>> assignCourseMap  = new HashMap<>();
//        Map<String,List<AssignTaskPlanTrack>> assignTestMap = new HashMap<>();
//        List<PlanDto> planDtoList = new ArrayList<>();
//        Date date  = new Date();
//        if(assignTask != null) {
//            List<Plan> plans = assignTask.getPlans();
//            List<CourseDto> courseDtos = new ArrayList<>();
//            List<String> courseIds = new ArrayList<>();
//            List<String> testIds = new ArrayList<>();
//            for (Plan plan : plans) {
//                for (Phase phase : plan.getPhases()) {
//                    for (PlanTask planTask : phase.getTasks()) {
//                        String _id = ((AssignTaskPlanTrack) planTask.getPlan()).get_id();
//                        if(planTask.getPlanType() != 3)
//                            planDtoList.add(PlanDto.builder().name(phase.getPhaseName()).date(formatter.format(date)).isComplete(planTask.getIsCompleted())
//                                .phase(_id).type(planTask.getPlanType()).build());
//                        //AssignTaskPlanTrack assignTaskPlanTrack = ((AssignTaskPlanTrack) task.getPlan());
//                        List<AssignTaskPlanTrack> milestones = (List<AssignTaskPlanTrack>) planTask.getPhases();
//                        if(planTask.getPlanType() == 2){
//                            assignTestMap.put(_id,milestones);
//                            testIds.add(_id);
//                        }
//                        if(planTask.getPlanType() != 1) continue;
//                        int count = 0;
//                        for (AssignTaskPlanTrack milestone : milestones) {
//                            if (milestone.getIsCompleted() == true)
//                                ++count;
//                        }
//                        courseIds.add(_id);
//                        courseDtos.add(new CourseDto(_id, count*100/Integer.max(1,milestones.size())));
//                        assignCourseMap.put(_id,milestones);
//                        // courseId.put(_id,count/ milestones.size() * 100);
//                    }
//                }
//            }
//            Map<String, Document> courseDetails = feedbackService.getCourseNameAndPhaseName(courseIds);
//            Map<String,Document> testDetails = feedbackService.getTestNameAndMilestoneName(testIds);
//            for (CourseDto courseDto : courseDtos) {
//                String _id = courseDto.getName();
//                String tp = (String) courseDetails.get(_id).get("name");
//                courseDto.setName(tp);
//            }
//            for(PlanDto planDto : planDtoList){
//                String _id = planDto.getPhase();
//                int type = Integer.min(3.planDt)
//                if(planDto.getType()==1||planDto.getType()==4){
//                    String tp = (String) courseDetails.get(_id).get("name");
//                    planDto.setPhase(tp);
//                }else if(planDto.getType()==2){
//                    String tp = (String) testDetails.get(_id).get("testName");
//                    planDto.setPhase(tp);
//                }
//            }
//            Collections.sort(planDtoList);
//            dashboardResponse.setCourses(courseDtos);
//            dashboardResponse.setPlan(planDtoList);
            //---set plan details!!!
//        }
        return dashboardResponse;
    }
}
