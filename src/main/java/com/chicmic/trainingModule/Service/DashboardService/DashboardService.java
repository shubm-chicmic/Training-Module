package com.chicmic.trainingModule.Service.DashboardService;


import com.chicmic.trainingModule.Dto.DashboardDto.*;
import com.chicmic.trainingModule.Dto.TimeTrack;
import com.chicmic.trainingModule.Dto.UserDto;
import com.chicmic.trainingModule.Entity.AssignedPlan;
import com.chicmic.trainingModule.Entity.Constants.PlanType;
import com.chicmic.trainingModule.Entity.PlanTask;
import com.chicmic.trainingModule.Entity.UserProgress;
import com.chicmic.trainingModule.Service.CourseServices.CourseService;
import com.chicmic.trainingModule.Service.FeedBackService.FeedbackService;
import com.chicmic.trainingModule.Service.PhaseService;
import com.chicmic.trainingModule.Service.TestServices.TestService;
import com.chicmic.trainingModule.Service.UserTimeService.UserTimeService;
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

import static com.chicmic.trainingModule.Entity.Constants.EntityType.COURSE;
import static com.chicmic.trainingModule.Entity.Constants.EntityType.TEST;

@Service
public class DashboardService {
    private final FeedbackService feedbackService;
    private final MongoTemplate mongoTemplate;
    private final TestService testService;
    private final CourseService courseService;
    private final PhaseService phaseService;
    private final UserTimeService userTimeService;

    public DashboardService(FeedbackService feedbackService, MongoTemplate mongoTemplate, TestService testService, CourseService courseService, PhaseService phaseService, UserTimeService userTimeService) {
        this.feedbackService = feedbackService;
        this.mongoTemplate = mongoTemplate;
        this.testService = testService;
        this.courseService = courseService;
        this.phaseService = phaseService;
        this.userTimeService = userTimeService;
    }

    public DashboardResponse getTraineeRatingSummary(String traineeId) {
        //feedbackService.testAggregationQuery("dfasf");
        //checking traineeId is valid or not
        UserDto userDto = TrainingModuleApplication.searchUserById(traineeId);

        DashboardResponse dashboardResponse = DashboardResponse.builder().build();
        dashboardResponse.setName(userDto.getName());

        RatingReponseDto ratingReponseDto = feedbackService.getOverallRatingOfTraineeForDashboard(traineeId);
        List<FeedbackResponseDto> feedbackResponseDtoList = feedbackService.findFirstFiveFeedbacksOfTrainee(traineeId);

        dashboardResponse.setRating(ratingReponseDto);
        dashboardResponse.setFeedbacks(feedbackResponseDtoList);

//        findCoursesAndTestsOfTrainee();

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
        Map<String,Map<String,Integer>> planCourseIds = new HashMap<>();
        List<String> courseIds = new ArrayList<>();
        List<String> testIds = new ArrayList<>();

        Aggregation aggregation1 = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("traineeId").is(traineeId).and("status").is(3)),
                Aggregation.group("planTaskId")
                        .count().as("count"),
                Aggregation.project("count","planTaskId")
//                        .and("_id.planId").as("planId").and("_id.phaseId").as("phaseId")
        );
        AggregationResults<Document> aggregationResults1 = mongoTemplate.aggregate(aggregation1, "userProgress", Document.class);
        List<Document> documentList1 = aggregationResults1.getMappedResults();//--->completed!!!
        Map<String,Integer> planTaskProgress = new HashMap<>();
        documentList1.forEach(d ->
            planTaskProgress.put((String) d.get("_id") ,(Integer) d.get("count"))
        );

        plans.forEach((p)->{
            if (!p.getDeleted()) {
                Map<String, Integer> courseProgress = new LinkedHashMap<>();
                p.getPhases().forEach(ps -> {
                    ps.getTasks().forEach(pt -> {
                        if (pt != null && pt instanceof PlanTask && pt.getPlanType() == COURSE) {
                            int prog = courseProgress.get(pt.getPlan()) == null ? 0 : courseProgress.get(pt.getPlan());
                            courseProgress.put(pt.getPlan(), prog + phaseService.countTotalSubtask(pt.getMilestones()));//pt.getTotalTasks()
                            //courseDtoList.add(new CourseDto(pt.getPlan(), p.get_id(),pt.getTotalTasks()));
                            criteriaList.add(Criteria.where("planId").is(p.get_id()).and("traineeId").is(traineeId).and("progressType").is(5).and("courseId").is(pt.getPlan()).and("status").is(3));
                        }
                        if(pt!= null && pt instanceof  PlanTask){
                            PlanDto planDto = PlanDto.builder()
                                    .name(p.getPlanName())
                                    .taskName(pt.getPlan())
                                    .subtasks(pt.getMilestones())
                                    .isComplete(false)
                                    .date(pt.getDate())
                                    .type(pt.getPlanType())
                                    .build();
                            if(pt.getPlanType() == PlanType.COURSE || pt.getPlanType() == PlanType.TEST) {
                                TimeTrack timeInPlanTask = userTimeService.getTimeForPlanTask(pt.get_id(), traineeId, p);
                                planDto.setExtraConsumedTime(Math.max((timeInPlanTask.getConsumedTime() - timeInPlanTask.getEstimatedTime()), 0));
                                planDto.setConsumedTime(timeInPlanTask.getConsumedTime());
                                planDto.setEstimatedTime(timeInPlanTask.getEstimatedTime());
                            }
                            planDto.setEstimatedTime(pt.getEstimatedTimeInSeconds());
                            planDtoList.add(planDto);
                                if(pt.getPlanType().equals(TEST)){
                                    int totalTask = phaseService.countTotalSubtask(pt.getMilestones());
                                    int completedTask = planTaskProgress.getOrDefault(pt.get_id(), 0);
                                    planDto.setComplete(totalTask == completedTask);
                                    testIds.add(pt.getPlan());
                                }
                                else{
                                    if(pt.getPlanType().equals(COURSE)){
                                        int totalTask = phaseService.countTotalSubtask(pt.getMilestones());
                                        int completedTask = planTaskProgress.getOrDefault(pt.get_id(), 0);
                                        planDto.setComplete(totalTask == completedTask);
                                    }else{
                                        planDto.setComplete(planTaskProgress.containsKey(pt.get_id()));
                                    }
                                    courseIds.add(pt.getPlan());
                                }
                        }
                    });
//                    planDtoList.add(PlanDto.builder().name(p.getPlanName()).phase(ps.getName())
//                            .isComplete(false).date(formatter.format(new Date())).type(ps.getEntityType()).build());
//                    planId.set(p.get_id());
                });
                for (Map.Entry<String, Integer> c : courseProgress.entrySet())
                    courseDtoList.add(new CourseDto(c.getKey(), p.get_id(), c.getValue(), 0, 0));
            }
        });

        //aggregation query!!!
        Criteria criteria2 = (!criteriaList.isEmpty())?new Criteria().orOperator(criteriaList):new Criteria();
        List<UserProgress> userProgresses = mongoTemplate.find(new Query(criteria2),UserProgress.class);

        //compute overall rating of a trainee!!!
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria2),
                Aggregation.group("courseId","planId","phaseId")
                        .count().as("count"),
                Aggregation.project("count").andExclude("_id").and("_id.courseId").as("courseId")
                        .and("_id.planId").as("planId").and("_id.phaseId").as("phaseId")
        );

        AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(aggregation, "userProgress", Document.class);
        List<Document> documentList = aggregationResults.getMappedResults();//--->completed!!!
        Map<String,Integer> progress = new HashMap<>();
        for (Document document : documentList){
            progress.put((String) document.get("courseId"),(Integer) document.get("count"));
        }

//        List<String> courseIds = new ArrayList<>();
//        courseDtoList.forEach(c -> courseIds.add(c.getName()));

        var testDetails = testService.findTestsByIds(testIds);
        var courseDetails = courseService.findCoursesByIds(courseIds);
        //get courses name
//        Criteria criteria1 = Criteria.where("_id").in(courseIds);
//        List<Course> courseList = mongoTemplate.find(new Query(criteria1), Course.class);
//        Map<String,String> positions = new HashMap<>();
//        for (int i=0;i<courseList.size();i++) positions.put(courseList.get(i).get_id(),courseList.get(i).getName());
        courseDtoList.forEach(c -> {
            //c.setProgress(0);
            int total = c.getProgress();
            c.setProgress(0);
            String planId = c.getPlanId();
            String courseId = c.getName();

            documentList.forEach(d ->{
                if (c.getPlanId().equals((String) d.get("planId")) && Objects.equals(c.getName(), (String) d.get("courseId")) ){
                    int completed = (d.get("count")==null)?0:(Integer) d.get("count");
                    if(completed > total) completed = total;
                    if (total!=0)  c.setProgress(Math.round(completed * 100.0f / total));
                }
            });
            TimeTrack courseTrack = userTimeService.getTimeForCourseInsidePlan(courseId, planId, traineeId);
            c.setConsumedTime(courseTrack.getConsumedTime());
            c.setEstimatedTime(courseTrack.getEstimatedTime());
        });

        courseDtoList.forEach(c -> c.setName(courseDetails.get(0).get(c.getName())));
        dashboardResponse.setCourses(courseDtoList);
        dashboardResponse.setPlan(planDtoList);
        planDtoList.forEach(pd ->{
            String taskId = pd.getTaskName();
            if(pd.getType() == 1 || pd.getType() == 3 || pd.getType() == 4){
                pd.setTaskName(courseDetails.get(0).get(taskId));
                List<Object> subTaskIds = new ArrayList<>();
                if(pd.getSubtasks() != null) {
                    pd.getSubtasks().forEach(st -> {
                        String subTaskId = (String) st;
                        subTaskIds.add(courseDetails.get(1).get(subTaskId));
                    });
                }
                pd.setSubtasks(subTaskIds);
            }else if (pd.getType() == TEST){
                pd.setTaskName(testDetails.get(0).get(taskId));
                List<Object> subTaskIds = new ArrayList<>();
                pd.getSubtasks().forEach(st->{
                    String subTaskId = (String) st;
                    subTaskIds.add(testDetails.get(1).get(subTaskId));
                });
                pd.setSubtasks(subTaskIds);
            }
        });
        return dashboardResponse;
    }

}
