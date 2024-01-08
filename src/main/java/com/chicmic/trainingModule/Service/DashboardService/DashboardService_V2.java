package com.chicmic.trainingModule.Service.DashboardService;


import com.chicmic.trainingModule.Dto.DashboardDto.CourseDto;
import com.chicmic.trainingModule.Dto.DashboardDto.DashboardResponse;
import com.chicmic.trainingModule.Dto.DashboardDto.PlanDto;
import com.chicmic.trainingModule.Dto.UserDto;
import com.chicmic.trainingModule.Entity.*;
import com.chicmic.trainingModule.Service.FeedBackService.FeedbackService_V2;
import com.chicmic.trainingModule.TrainingModuleApplication;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static com.chicmic.trainingModule.Entity.Constants.EntityType.COURSE;

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
        List<Criteria> criteriaList = new ArrayList<>();

        plans.forEach((p)->{
            p.getPhases().forEach(ps -> {
                ps.getTasks().forEach(pt -> {
                    if (pt != null && pt instanceof PlanTask && pt.getPlanType() == COURSE) {
                        courseDtoList.add(new CourseDto(pt.getPlan(), p.get_id(),pt.getTotalTasks()));
                        criteriaList.add(Criteria.where("planId").is(p.get_id()).and("traineeId").is(traineeId).and("progressType").is(5).and("courseId").is(pt.getPlan()));
                    }
                });
                planDtoList.add(PlanDto.builder().name(p.getPlanName()).phase(ps.getName())
                        .isComplete(false).date(formatter.format(new Date())).build());
                    //planId.set(p.get_id());
            });
        });
        //aggregation query!!!
        Criteria criteria2 = new Criteria().orOperator(criteriaList);
        List<UserProgress> userProgresses = mongoTemplate.find(new Query(criteria2),UserProgress.class);

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria2),
                Aggregation.group("courseId","planId")
                        .count().as("count"),
                Aggregation.project("count").andExclude("_id").and("_id.courseId").as("courseId")
                        .and("_id.planId").as("planId")
        );
        AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(aggregation, "userProgress", Document.class);
        List<Document> documentList = aggregationResults.getMappedResults();
        Map<String,Integer> progress = new HashMap<>();
        for (Document document : documentList){
            progress.put((String) document.get("courseId"),(Integer) document.get("count"));
        }

        List<String> courseIds = new ArrayList<>();
        courseDtoList.forEach(c -> courseIds.add(c.getName()));
        //get courses name
        Criteria criteria1 = Criteria.where("_id").in(courseIds);
        List<Course> courseList = mongoTemplate.find(new Query(criteria1), Course.class);
        Map<String,String> positions = new HashMap<>();
        for (int i=0;i<courseList.size();i++) positions.put(courseList.get(i).get_id(),courseList.get(i).getName());
        courseDtoList.forEach(c -> {
            documentList.forEach(d ->{
                if (c.getPlanId().equals((String) d.get("planId")) && Objects.equals(c.getName(), (String) d.get("courseId"))){
                    int completed = (d.get("count")==null)?0:(Integer) d.get("count");
                    int total = c.getProgress();
                    c.setProgress(completed * 100 / total);
                }
            });
        });
        courseDtoList.forEach(c -> c.setName(positions.get(c.getName())));
        dashboardResponse.setCourses(courseDtoList);
        dashboardResponse.setPlan(planDtoList);
        return dashboardResponse;
    }
}
