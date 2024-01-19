package com.chicmic.trainingModule.Service.FeedBackService;

import com.chicmic.trainingModule.Dto.*;
import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponse;
import com.chicmic.trainingModule.Dto.CourseResponse_V2.CourseResponse_V2;
import com.chicmic.trainingModule.Dto.DashboardDto.*;
import com.chicmic.trainingModule.Dto.FeedbackDto.FeedbackRequestDto;
import com.chicmic.trainingModule.Dto.FeedbackDto.RatingAndCountDto;
import com.chicmic.trainingModule.Dto.FeedbackResponseDto_V2.FeedbackResponse;
import com.chicmic.trainingModule.Dto.PhaseResponse_V2.PhaseResponse_V2;
import com.chicmic.trainingModule.Dto.rating.Rating;
import com.chicmic.trainingModule.Dto.rating.Rating_COURSE;
import com.chicmic.trainingModule.Dto.rating.Rating_PPT;
import com.chicmic.trainingModule.Dto.rating.Rating_TEST;
import com.chicmic.trainingModule.Entity.*;
import com.chicmic.trainingModule.ExceptionHandling.ApiException;
import com.chicmic.trainingModule.Service.CourseServices.CourseService;
import com.chicmic.trainingModule.Service.PlanServices.PlanService;
import com.chicmic.trainingModule.Service.TestServices.TestService;
import com.chicmic.trainingModule.TrainingModuleApplication;
import org.bson.Document;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.chicmic.trainingModule.Dto.FeedbackResponseDto_V2.FeedbackResponse.buildFeedbackResponse;
import static com.chicmic.trainingModule.Entity.Feedback_V2.buildFeedbackFromFeedbackRequestDto;
import static com.chicmic.trainingModule.TrainingModuleApplication.idUserMap;
import static com.chicmic.trainingModule.Util.FeedbackUtil.getFeedbackMessageBasedOnOverallRating;
import static com.chicmic.trainingModule.Util.RatingUtil.roundOff_Rating;
import static com.chicmic.trainingModule.Util.TrimNullValidator.FeedbackType.*;
import static com.mongodb.client.model.Aggregates.facet;
import static java.util.Arrays.asList;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;
import org.springframework.data.domain.Sort;
@Service
public class FeedbackService_V2 {
    private final MongoTemplate mongoTemplate;
    private final TestService testService;
    private final CourseService courseService;
    private final PlanService planService;

    public FeedbackService_V2(MongoTemplate mongoTemplate, TestService testService, CourseService courseService, PlanService planService) {
        this.mongoTemplate = mongoTemplate;
        this.testService = testService;
        this.courseService = courseService;
        this.planService = planService;
    }

    public List<String> checkTaskAssignedOrNot(FeedbackRequestDto feedbackRequestDto){
        String traineeId = feedbackRequestDto.getTrainee();
        String taskId = (feedbackRequestDto.getFeedbackType().equals(TEST_))?feedbackRequestDto.getTest():feedbackRequestDto.getCourse();
        HashSet<String> subTaskId = (feedbackRequestDto.getFeedbackType().equals(TEST_))?feedbackRequestDto.getMilestone():feedbackRequestDto.getPhase();
        int type = feedbackRequestDto.getFeedbackType().charAt(0) - '0';

        AssignedPlan assignedPlan = mongoTemplate.findOne(new Query(Criteria.where("userId").is(traineeId)),AssignedPlan.class);
        if(assignedPlan == null||assignedPlan.getPlans() == null||assignedPlan.getPlans().isEmpty())
            throw new ApiException(HttpStatus.BAD_REQUEST,"No plan Assigned!!");
        List<Plan> plans = assignedPlan.getPlans();
        List<String> mentorIds = new ArrayList<>();
        Set<Object> phaseIds = new HashSet<>();
        AtomicInteger value = new AtomicInteger(0);
        plans.forEach(p->{
            var phases = p.getPhases();
            if(phases == null) throw new ApiException(HttpStatus.BAD_REQUEST,"Task not assigned!!!");
            phases.forEach(ph ->{
                var pt = ph.getTasks();
                if(pt == null) throw new ApiException(HttpStatus.BAD_REQUEST,"Task not assigned!!!");
                pt.forEach(ptask ->{
                    if(ptask.getPlanType() == type) {
                        if (Objects.equals(ptask.getPlanType(), VIVA) && ptask.getPlan().equals(taskId)) {
                            value.set(1);
                            mentorIds.addAll(ptask.getMentorIds());
                            phaseIds.addAll(ptask.getMilestones());
                        } else if (Objects.equals(ptask.getPlanType(), TEST) && ptask.getPlan().equals(taskId)) {
                            value.set(1);
                            mentorIds.addAll(ptask.getMentorIds());
                            phaseIds.addAll(ptask.getMilestones());
                        } else if (Objects.equals(ptask.getPlanType(), PPT) && ptask.getPlan().equals(taskId)) {
                            value.set(1);
                            mentorIds.addAll(ptask.getMentorIds());
                        }
                    }
                });
            });
        });

        if(!feedbackRequestDto.getFeedbackType().equals(PPT_)) {
            if(phaseIds.isEmpty())
                throw new ApiException(HttpStatus.BAD_REQUEST, "Task not assigned!!!");
            HashSet<String> taskName = new HashSet<>();
            phaseIds.forEach(ph -> {
                taskName.add((String) ph);
            });
            boolean flag = taskName.containsAll(subTaskId);
            if (!flag)
                throw new ApiException(HttpStatus.BAD_REQUEST, "Task not assigned!!!");
        }

        if(value.get() == 1)
            return mentorIds;

        throw new ApiException(HttpStatus.BAD_REQUEST,"Task not assigned!!!");
    }

    public boolean feedbackExist(Feedback_V2 feedback_v2, String reviewer){
        Criteria criteria = Criteria.where("traineeId").is(feedback_v2.getTraineeId())
                .and("createdBy").is(reviewer).and("type").is(feedback_v2.getType())
                .and("isDeleted").is(false);

        //String taskId = feedback_v2.getDetails().getTaskId();
        String courseId = feedback_v2.getDetails().getCourseId();
        if(courseId != null)
            criteria.and("details.courseId").is(courseId);

        String testId = feedback_v2.getDetails().getTestId();
        if(testId != null)
                criteria.and("details.testId").is(testId);
        String planId = feedback_v2.getPlanId();
        if(planId != null)
            criteria.and("planId").is(planId);
        if (feedback_v2.getPhaseIds() != null)
            criteria.and("phaseIds").is(feedback_v2.getPhaseIds());
        if (feedback_v2.getMilestoneIds() != null)
            criteria.and("milestoneIds").is(feedback_v2.getMilestoneIds());

        return mongoTemplate.exists(new Query(criteria), Feedback_V2.class);
    }

    public FeedbackResponse saveFeedbackInDb(FeedbackRequestDto feedbackDto, String reviewerId,boolean role){
        //checking traineeId is valid
        TrainingModuleApplication.searchUserById(feedbackDto.getTrainee());

        if(!feedbackDto.getFeedbackType().equals(BEHAVIUOR_)) {
            List<String> mentors = checkTaskAssignedOrNot(feedbackDto);
            if (!role && !mentors.contains(reviewerId))
                throw new ApiException(HttpStatus.BAD_REQUEST, "You can't give feedback to this trainee!!");
        }

        Feedback_V2 feedback = buildFeedbackFromFeedbackRequestDto(feedbackDto,reviewerId);

        Query query = new Query(Criteria.where("_id").is(feedback.getPlanId()));
        query.fields().include("planName");

        Plan plan = mongoTemplate.findOne(query,Plan.class);
        if(plan == null && !feedbackDto.getFeedbackType().equals(BEHAVIUOR_)) throw new ApiException(HttpStatus.BAD_REQUEST,"Please enter valid planId!!");
        //course assigned or not!!

        //checking feedback exist or not!!
        boolean flag = feedbackExist(feedback,reviewerId);
        if (flag) throw new ApiException(HttpStatus.BAD_REQUEST,"Feedback submitted previously!!");

        Feedback_V2 feedbackV2 =  mongoTemplate.insert(feedback,"feedback_V2");
        FeedbackResponse feedbackResponse = buildFeedbackResponse(feedbackV2);
        if (!feedbackV2.getType().equals(BEHAVIUOR_))
            feedbackResponse.setPlan(new UserIdAndNameDto(feedback.getPlanId(), plan.getPlanName()));
        addTaskNameAndSubTaskName(asList(feedbackResponse));
        return feedbackResponse;
    }

    public Feedback_V2 saveTraineeFeedback(FeedbackRequestDto feedbackDto, String reviewerId,boolean role){
        //checking traineeId is valid
        TrainingModuleApplication.searchUserById(feedbackDto.getTrainee());
        if(!feedbackDto.getFeedbackType().equals(BEHAVIUOR_)) {
            List<String> mentors = checkTaskAssignedOrNot(feedbackDto);
            if (!role && !mentors.contains(reviewerId))
                throw new ApiException(HttpStatus.BAD_REQUEST, "You can't give feedback to this trainee!!");
        }

        Feedback_V2 feedback = buildFeedbackFromFeedbackRequestDto(feedbackDto,reviewerId);

        Query query = new Query(Criteria.where("_id").is(feedback.getPlanId()));
        query.fields().include("planName");

        Plan plan = mongoTemplate.findOne(query,Plan.class);
        if(plan == null && !feedbackDto.getFeedbackType().equals(BEHAVIUOR_)) throw new ApiException(HttpStatus.BAD_REQUEST,"Please enter valid planId!!");
        //course assigned or not!!

        //checking feedback exist or not!!
        boolean flag = feedbackExist(feedback,reviewerId);
        if (flag) throw new ApiException(HttpStatus.BAD_REQUEST,"Feedback submitted previously!!");

        Feedback_V2 feedbackV2 =  mongoTemplate.insert(feedback,"feedback_V2");
        return feedbackV2;
    }

    public FeedbackResponse updateFeedback(FeedbackRequestDto feedbackRequestDto,String reviewerId){
        //getting feedback type!!!
        //String type = FEEDBACK_TYPE_CATEGORY_V2[feedbackRequestDto.getFeedbackType().charAt(0) - '1'];

        Criteria criteria = Criteria.where("_id").is(feedbackRequestDto.get_id())
                .and("createdBy").is(reviewerId).and("type").is(feedbackRequestDto.getFeedbackType());

        //find feedback!!!
        Feedback_V2 feedbackV2 = mongoTemplate.findOne(new Query(criteria),Feedback_V2.class);
        if(feedbackV2 == null)
            throw new ApiException(HttpStatus.BAD_REQUEST,"You can't update this feedback!!");

        Query query = new Query(Criteria.where("_id").is(feedbackV2.getPlanId()));
        query.fields().include("planName");

        Plan plan = mongoTemplate.findOne(query,Plan.class);
        if(plan == null && !feedbackV2.getType().equals(BEHAVIUOR_)) throw new ApiException(HttpStatus.BAD_REQUEST,"Please enter valid planId!!");

        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");


        Rating rating = Rating.getRating(feedbackRequestDto);
        Update update = new Update()
                .set("updateAt",date)
                .set("details",rating)
                .set("comment",feedbackRequestDto.getComment())
                .set("overallRating",compute_rating(feedbackRequestDto.computeRating(),1));

       FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true);

        Feedback_V2 feedback_v2 = mongoTemplate.findAndModify(new Query(criteria),update,options,Feedback_V2.class);
        FeedbackResponse feedbackResponse = buildFeedbackResponse(feedback_v2);
        if (!feedbackV2.getType().equals(BEHAVIUOR_))
            feedbackResponse.setPlan(new UserIdAndNameDto(feedback_v2.getPlanId(), plan.getPlanName()));
        addTaskNameAndSubTaskName(asList(feedbackResponse));
        return feedbackResponse;
    }

    public Feedback_V2 deleteFeedbackById(String _id,String reviewerId){
        Criteria criteria = Criteria.where("_id").is(_id).and("createdBy").is(reviewerId);
        Query query = new Query(criteria);
        Update update = new Update().set("isDeleted",true);
        Feedback_V2 feedback = mongoTemplate.findAndModify(query,update,Feedback_V2.class);
        if(feedback == null)  throw new ApiException(HttpStatus.valueOf(400),"You can't delete this feedback");

        return feedback;
    }
    public FeedbackResponse_V2 getFeedbackById(String _id){
//        Feedback_V2 feedbackV2 =  mongoTemplate.findOne(new Query(criteria), Feedback_V2.class);
        Feedback_V2 feedbackV2 =  mongoTemplate.findById(_id, Feedback_V2.class);
        if (feedbackV2 == null)
            throw new ApiException(HttpStatus.BAD_REQUEST,"Feedback doesn't exist!!");

        Query query = new Query(Criteria.where("_id").is(feedbackV2.getPlanId()));
        query.fields().include("planName");

        Plan plan = mongoTemplate.findOne(query,Plan.class);
        if(plan == null && !feedbackV2.getType().equals(BEHAVIUOR_)) throw new ApiException(HttpStatus.BAD_REQUEST,"Invalid planId!!");

        FeedbackResponse_V2 feedbackResponseV2 = FeedbackResponse_V2.buildResponse(feedbackV2);

        if (!feedbackV2.getType().equals(BEHAVIUOR_))
            feedbackResponseV2.setPlan(new UserIdAndNameDto(feedbackV2.getPlanId(), plan.getPlanName()));

       // addTaskNameAndSubTaskName(Arrays.asList(feedbackResponseV2))
        addTaskNameAndSubTaskName(feedbackResponseV2);
        return feedbackResponseV2;
    }

    public ApiResponse findFeedbacksOnUserPlan(String traineeId,String planId,Integer pageNumber, Integer pageSize,String query,Integer sortDirection,String sortKey){
//        List<Criteria> criteriaList = getAllTaskIdsInPlan(traineeId,planId);
        List<Document> userDatasDocuments = idUserMap.values().stream().map(userDto ->
                        new Document("reviewerName",userDto.getName()).append("reviewerTeam",userDto.getTeamName()).append("reviewerCode",userDto.getEmpCode())
                                .append("id",userDto.get_id()))
                .toList();

//        Criteria criteria = Criteria.where("traineeId").is(traineeId)
//                .and("isDeleted").is(false);
//        Criteria criteria = new Criteria().orOperator(criteriaList);
        Criteria criteria = Criteria.where("traineeId").is(traineeId).and("planId").is(planId).and("isDeleted").is(false);
        int skipValue = pageNumber;//(pageNumber - 1) * pageSize;
        if(query==null || query.isBlank()) query = ".*";
        java.util.regex.Pattern namePattern = java.util.regex.Pattern.compile(query, java.util.regex.Pattern.CASE_INSENSITIVE);
        
        Aggregation aggregation = newAggregation(
                match(criteria),
                context -> new Document("$addFields", new Document("userDatas", userDatasDocuments)),
                context -> new Document("$addFields", new Document("userData",
                        new Document("$filter",
                                new Document("input", "$userDatas")
                                        .append("as", "user")
                                        .append("cond", new Document("$eq", asList("$$user.id", "$createdBy")))
                        )
                )),
                context -> new Document("$unwind",
                        new Document("path", "$userData")
                                .append("preserveNullAndEmptyArrays", true)
                ),
                context -> new Document("$project", new Document("userDatas", 0)),
                context -> new Document("$match", new Document("$or", asList(
                        new Document("userData.reviewerName", new Document("$regex", namePattern))
                ))),
                context -> new Document("$sort", new Document(sortKey, sortDirection)),
                context -> new Document("$skip", Integer.max(skipValue,0)), // Apply skip to paginate
                context -> new Document("$limit", pageSize)
        );

        // Execute the aggregation
        List<Feedback_V2> feedbackList = mongoTemplate.aggregate(aggregation, "feedback_V2", Feedback_V2.class).getMappedResults();
        List<FeedbackResponse> feedbackResponse_v2List = new ArrayList<>();
        for (Feedback_V2 feedbackV2 : feedbackList)
            feedbackResponse_v2List.add(buildFeedbackResponse(feedbackV2));

        addTaskNameAndSubTaskName(feedbackResponse_v2List);
        long count = mongoTemplate.count(new Query(criteria),Feedback_V2.class);

        return new ApiResponse(200,"List of All feedbacks",feedbackResponse_v2List,count);
    }

    public List<Criteria> getAllTaskIdsInPlan(String traineeId,String planId){
        Criteria criteria = Criteria.where("_id").is(planId);
        Query query = new Query(criteria);
        Plan plan = mongoTemplate.findOne(query,Plan.class);
//        List<TaskIdAndTypeDto> ids = new ArrayList<>();
        List<Criteria> criteriaList = new ArrayList<>();
        if (plan == null)
            throw new ApiException(HttpStatus.BAD_REQUEST,"Please enter valid PlanId!!");
        plan.getPhases().forEach(ph ->{
            if(ph != null){
               ph.getTasks().forEach(tk ->{
                   if(tk != null) {
                       String taskId = (tk.getPlanType().equals(TEST))?"testId":"courseId";
                       criteriaList.add(Criteria.where("type").is(tk.getPlanType().toString()).and(String.format("details.%s",taskId)).is(tk.getPlan()).and("traineeId").is(traineeId).and("isDeleted").is(false));
                   }
               });
            }
        });
        return criteriaList;
    }
    public boolean feedbackExistOnParticularPhaseOfTrainee(String traineeId,String taskId,List<String> subtaskIds,String type){
        Criteria criteria = Criteria.where("traineeId").is(traineeId).and("type").is(type);
        if (type.equals(VIVA_)||type.equals(PPT_)) {
            criteria.and("details.courseId").is(taskId);
            if (type.equals(VIVA_))
                criteria.and("phaseIds").all(subtaskIds);
        } else if (type.equals(TEST_)) {
            criteria.and("details.testId").is(taskId);
            criteria.and("milestoneIds").all(subtaskIds);
        }
        return mongoTemplate.exists(new Query(criteria),Feedback_V2.class);
    }

    public ApiResponse findTraineeFeedbacks(Integer pageNumber, Integer pageSize, String query, Integer sortDirection, String sortKey, String traineeId) {
        List<Document> userDatasDocuments = idUserMap.values().stream().map(userDto ->
                        new Document("reviewerName",userDto.getName()).append("reviewerTeam",userDto.getTeamName()).append("reviewerCode",userDto.getEmpCode())
                                .append("id",userDto.get_id()))
                .toList();

        Criteria criteria = Criteria.where("traineeId").is(traineeId)
                .and("isDeleted").is(false);
//        //searching!!!
        if(query==null || query.isBlank()) query = ".*";
        int skipValue = pageNumber;//(pageNumber - 1) * pageSize;
        java.util.regex.Pattern namePattern = java.util.regex.Pattern.compile(query, java.util.regex.Pattern.CASE_INSENSITIVE);
        Aggregation aggregation = newAggregation(
                match(criteria),
                context -> new Document("$addFields", new Document("userDatas", userDatasDocuments)),
                context -> new Document("$addFields", new Document("userData",
                        new Document("$filter",
                                new Document("input", "$userDatas")
                                        .append("as", "user")
                                        .append("cond", new Document("$eq", asList("$$user.id", "$createdBy")))
                        )
                )),
                context -> new Document("$unwind",
                        new Document("path", "$userData")
                                .append("preserveNullAndEmptyArrays", true)
                ),
                context -> new Document("$project", new Document("userDatas", 0)),
                context -> new Document("$match", new Document("$or", asList(
                        new Document("userData.reviewerName", new Document("$regex", namePattern)),
                        new Document("userData.reviewerTeam",new Document("$regex",namePattern))// Search by 'team' field, without case-insensitive regex
                ))),

                context -> new Document("$sort", new Document(sortKey, sortDirection)),
                context -> new Document("$skip", Integer.max(skipValue,0)), // Apply skip to paginate
                context -> new Document("$limit", pageSize)
        );

        // Execute the aggregation
        List<Feedback_V2> feedbackList = mongoTemplate.aggregate(aggregation, "feedback_V2", Feedback_V2.class).getMappedResults();
        List<FeedbackResponse> feedbackResponse_v2List = new ArrayList<>();
        for (Feedback_V2 feedbackV2 : feedbackList) {
            FeedbackResponse feedbackResponse = buildFeedbackResponse(feedbackV2);
            if (!feedbackV2.getType().equals(BEHAVIUOR_))
                feedbackResponse.setPlan(new UserIdAndNameDto(feedbackV2.getPlanId(),feedbackV2.getPlanId()));
            feedbackResponse_v2List.add(feedbackResponse);
        }


        addTaskNameAndSubTaskName(feedbackResponse_v2List);
        long count = mongoTemplate.count(new Query(criteria),Feedback_V2.class);
        Double overallRating = computeOverallRatingOfTrainee(traineeId);
        return new ApiResponse(200,"List of All feedbacks",feedbackResponse_v2List,count,overallRating);
    }

    public ApiResponse findFeedbacksGivenByUser(Integer pageNumber, Integer pageSize, String query, Integer sortDirection, String sortKey,String reviewer){
        List<Document> userDatasDocuments = idUserMap.values().stream().map(userDto ->
                        new Document("traineeName",userDto.getName()).append("traineeTeam",userDto.getTeamName()).append("traineeCode",userDto.getEmpCode())
                                .append("id",userDto.get_id()))
                .toList();

        Criteria criteria = Criteria.where("createdBy").is(reviewer)
                .and("isDeleted").is(false);
      //  System.out.println("Control reaches here!!!------------");
//       //searching!!!
        if(query==null || query.isBlank()) query = ".*";
        int skipValue = pageNumber;//(pageNumber - 1) * pageSize;

//        System.out.println(userDatasDocuments.size() + "///");
        java.util.regex.Pattern namePattern = java.util.regex.Pattern.compile(query, java.util.regex.Pattern.CASE_INSENSITIVE);
        Aggregation aggregation = newAggregation(
                match(criteria),
                context -> new Document("$addFields", new Document("userDatas", userDatasDocuments)),
                context -> new Document("$addFields", new Document("userData",
                        new Document("$filter",
                                new Document("input", "$userDatas")
                                        .append("as", "user")
                                        .append("cond", new Document("$eq", asList("$$user.id", "$traineeId")))
                        )
                )),
                context -> new Document("$unwind",
                        new Document("path", "$userData")
                                .append("preserveNullAndEmptyArrays", true)
                ),
                context -> new Document("$project", new Document("userDatas", 0)),
                context -> new Document("$match", new Document("$or", asList(
                        new Document("userData.traineeName", new Document("$regex", namePattern)),
                        new Document("userData.traineeTeam",new Document("$regex",namePattern))// Search by 'team' field, without case-insensitive regex
                ))),
                context -> new Document("$sort", new Document(sortKey, sortDirection)),
                context -> new Document("$skip", Integer.max(skipValue,0)), // Apply skip to paginate
                context -> new Document("$limit", pageSize)
        );

        // Execute the aggregation
        List<Feedback_V2> feedbackList = mongoTemplate.aggregate(aggregation, "feedback_V2", Feedback_V2.class).getMappedResults();

        List<FeedbackResponse> feedbackResponse_v2List = new ArrayList<>();
        for (Feedback_V2 feedbackV2 : feedbackList)
            feedbackResponse_v2List.add(buildFeedbackResponse(feedbackV2));
        addTaskNameAndSubTaskName(feedbackResponse_v2List);
        long count = mongoTemplate.count(new Query(criteria),Feedback_V2.class);
        addTaskNameAndSubTaskName(feedbackResponse_v2List);
        return new ApiResponse(200,"List of All feedbacks",feedbackResponse_v2List,count);
    }


    public void addTaskNameAndSubTaskName(List<FeedbackResponse> feedbackResponseList){
        List<String> courseIds = new ArrayList<>();
        List<String> testIds = new ArrayList<>();
        List<String> planIds = new ArrayList<>();
        feedbackResponseList.forEach(f ->{
            if (f.getFeedbackType().get_id().equals(VIVA_) || f.getFeedbackType().get_id().equals(PPT_))
                courseIds.add(f.getTask().get_id());
            else if (f.getFeedbackType().get_id().equals(TEST_)){
                testIds.add(f.getTask().get_id());
            }
            //update condition!!!
            if(f.getPlan() != null)//if (!f.getFeedbackType().get_id().equals(BEHAVIUOR_))
                planIds.add(f.getPlan().get_id());
        });
        var testDetails = testService.findTestsByIds(testIds);
        var courseDetails = courseService.findCoursesByIds(courseIds);
        var planDetails = planService.getPlanName(planIds);

        feedbackResponseList.forEach(f->{
            if (f.getFeedbackType().get_id().equals(VIVA_) || f.getFeedbackType().get_id().equals(PPT_)){
                f.getTask().setName(courseDetails.get(0).get(f.getTask().get_id()));
                //adding phaseName
                if(f.getFeedbackType().get_id().equals(VIVA_))
                    f.getSubTask().forEach( p -> p.setName(courseDetails.get(1).get(p.get_id())));
            }
            else if (f.getFeedbackType().get_id().equals(TEST_)) {
                f.getTask().setName(testDetails.get(0).get(f.getTask().get_id()));
                //adding milestone name
                f.getSubTask().forEach(m -> m.setName(testDetails.get(1).get(m.get_id())));
            }
            if (f.getPlan()!=null) {
                String planId = f.getPlan().get_id();
                f.getPlan().setName(planDetails.get(planId));
            }
        });
    }
    public void addTaskNameAndSubTaskName(FeedbackResponse_V2 f){
        List<String> courseIds = new ArrayList<>();
        List<String> testIds = new ArrayList<>();

        if (f.getFeedbackType() == VIVA || f.getFeedbackType() == PPT)
            courseIds.add(f.getCourse().get_id());
        else if (f.getFeedbackType() == TEST){
            testIds.add(f.getTest().get_id());
        }
        var testDetails = testService.findTestsByIds(testIds);
        var courseDetails = courseService.findCoursesByIds(courseIds);

        if (f.getFeedbackType() == VIVA || f.getFeedbackType() == PPT) {
            f.getCourse().setName(courseDetails.get(0).get(f.getCourse().get_id()));
            if (f.getFeedbackType() == VIVA) f.getPhase().forEach(p -> p.setName(courseDetails.get(1).get(p.get_id())));
        }
        else if (f.getFeedbackType() == TEST){
            f.getTest().setName(testDetails.get(0).get(f.getTest().get_id()));
            f.getMilestone().forEach(m -> m.setName(testDetails.get(1).get(m.get_id())));
        }
    }
    public List<CourseResponse_V2> findFeedbacksByTaskIdAndTraineeIdAndType(String _id,String traineeId,Integer type){
        Criteria criteria = Criteria.where("type").is(type.toString()).and("traineeId").is(traineeId);
        if (type.equals(VIVA) || type.equals(PPT))
            criteria.and("details.courseId").is(_id);
        else if(type.equals(TEST))
            criteria.and("details.testId").is(_id);
        Query query = new Query(criteria);
        List<Feedback_V2> feedbackV2List =  mongoTemplate.find(query,Feedback_V2.class);
        List<CourseResponse_V2> courseResponseV2List = buildFeedbackResponseForCourseAndTest(feedbackV2List);
        return courseResponseV2List;
    }

    public List<CourseResponse_V2> buildFeedbackResponseForCourseAndTest(List<Feedback_V2> feedbackList){
//        if(feedbackList == null || feedbackList.isEmpty())
//            return null;
        List<String> courseIds = new ArrayList<>();
        List<String> testIds = new ArrayList<>();
        feedbackList.forEach(f -> {
            if(f.getType().equals(VIVA_)||f.getType().equals(PPT_))
                courseIds.add(f.getDetails().getCourseId());
            else if(f.getType().equals(TEST_))
                testIds.add(f.getDetails().getTestId());
        });

        var testDetails = testService.findTestsByIds(testIds);
        var courseDetails = courseService.findCoursesByIds(courseIds);
        List<CourseResponse_V2> courseResponseList = new ArrayList<>();
        Map<String, RatingAndCountDto> reviewerDetails = new HashMap<>();
        HashMap<String, TraineeRating> dp = new HashMap<>();
//        List<Reviewer> courseResponseList = new ArrayList<>();
//        List<CourseResponse_V2> courseResponseList = new ArrayList<>();
        for(Feedback_V2 feedback : feedbackList){
            String reviewerId = feedback.getCreatedBy();
            if(dp.get(reviewerId) == null) {//not present
                UserDto reviewer = TrainingModuleApplication.searchUserById(reviewerId);

                //creating a new element for reviewer
                CourseResponse_V2 courseResponse = CourseResponse_V2.builder()
                        ._id(reviewerId)
                        .reviewerName(reviewer.getName())
                        .code(reviewer.getEmpCode())
                        .overallRating(5.00)
                        .records(new ArrayList<>())
                        .build();

                PhaseResponse_V2 phaseResponse = buildPhaseResponseForCourseOrTest(feedback,courseDetails.get(1),testDetails.get(1));
                //adding the phase into it
                courseResponse.getRecords().add(phaseResponse);
                dp.put(reviewerId,new TraineeRating(courseResponseList.size(),phaseResponse.getOverallRating(),1));
//                courseResponseList.add(new Reviewer(courseResponse));
                courseResponseList.add(courseResponse);
            }else{
                //int idx = dp.get(reviewerId).getIndex();
                TraineeRating traineeRating = dp.get(reviewerId);
                PhaseResponse_V2 phaseResponse = buildPhaseResponseForCourseOrTest(feedback,courseDetails.get(1),testDetails.get(1));
                //updating the count
                traineeRating.incrRating(phaseResponse.getOverallRating());
                traineeRating.incrCount();
//               courseResponseList.get(traineeRating.getIndex()).reviewer().getPhases().add(phaseResponse);
                courseResponseList.get(traineeRating.getIndex()).getRecords().add(phaseResponse);
                // courseResponseList.get(idx).reviewer().getPhases().add(phaseResponse);
            }
        }
        //set overall rating in list
        for (TraineeRating rating : dp.values()){
            int index = rating.getIndex();
//               courseResponseList.get(index).reviewer().setOverallRating(roundOff_Rating(rating.getRating()/rating.getCount()));
            courseResponseList.get(index).setOverallRating(compute_rating(rating.getRating(),rating.getCount()));
        }
        //courseResponseList = addSubTaskName(courseResponseList,);
        return courseResponseList;
    }

    public PhaseResponse_V2 buildPhaseResponseForCourseOrTest(Feedback_V2 feedback_v2,Map<String,String> phaseDetails,Map<String,String> milestoneDetails){
    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");    
    PhaseResponse_V2 phaseResponse = PhaseResponse_V2.builder()
                .comment(feedback_v2.getComment())
                .overallRating(compute_rating(feedback_v2.getDetails().computeOverallRating(),1))
                .createdAt(formatter.format(feedback_v2.getCreatedAt()))
                .subTask(new ArrayList<>())
                .build();
        if(feedback_v2.getType().equals(VIVA_)) {
            Rating_COURSE ratingCourse = (Rating_COURSE) feedback_v2.getDetails();
            feedback_v2.getPhaseIds().forEach(st -> { phaseResponse.getSubTask().add(new UserIdAndNameDto(st, phaseDetails.get(st)));});
            Collections.sort(phaseResponse.getSubTask());
//            phaseResponse.set_id(ratingCourse.getPhaseId());
//            phaseResponse.setName(name.get(ratingCourse.getPhaseId()));
            phaseResponse.setTheoreticalRating(ratingCourse.getTheoreticalRating());
            phaseResponse.setCommunicationRating(ratingCourse.getCommunicationRating());
            phaseResponse.setTechnicalRating(ratingCourse.getTechnicalRating());
        }else if(feedback_v2.getType().equals(TEST_)){
            Rating_TEST ratingTest = (Rating_TEST) feedback_v2.getDetails();
            feedback_v2.getMilestoneIds().forEach(st -> { phaseResponse.getSubTask().add(new UserIdAndNameDto(st,milestoneDetails.get(st)));});
            Collections.sort(phaseResponse.getSubTask());
//            phaseResponse.set_id(ratingTest.getMilestoneId());
//            phaseResponse.setName(name.get(ratingTest.getMilestoneId()));
            phaseResponse.setTheoreticalRating(ratingTest.getTheoreticalRating());
            phaseResponse.setCommunicationRating(ratingTest.getCommunicationRating());
            phaseResponse.setCodingRating(ratingTest.getCodingRating());
        }else if (feedback_v2.getType().equals(PPT_)){
            Rating_PPT rating_ppt = (Rating_PPT) feedback_v2.getDetails();
//            phaseResponse.set_id(rating_ppt.getCourseId());
//            phaseResponse.setName(name.get(rating_ppt.getCourseId()));
            phaseResponse.setPresentationRating(rating_ppt.getPresentationRating());
            phaseResponse.setCommunicationRating(rating_ppt.getCommunicationRating());
            phaseResponse.setTechnicalRating(rating_ppt.getTechnicalRating());
        }
        return phaseResponse;
    }

    public List<Document> calculateEmployeeRatingSummary(Set<String> userIds) {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("traineeId").in(userIds).and("isDeleted").is(false)),
                group("traineeId")
                        .sum("overallRating").as("overallRating")
                        .count().as("count")
        );
        AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(aggregation, "feedback_V2", Document.class);
        return aggregationResults.getMappedResults();
    }
    public List<CourseResponse_V2> findFeedbacksByTaskIdAndTraineeId(String taskId,String planId,String traineeId,int feedbackType){
        PlanTask planTask = findMilestonesFromPlanTask(taskId);
        planTask.getMilestones().forEach(System.out::println);
        if(planTask == null)
            throw new ApiException(HttpStatus.BAD_REQUEST,"PlanTask not found!!");

        Criteria criteria = Criteria.where("traineeId").is(traineeId).and("type").is(Integer.toString(feedbackType))
                .and("isDeleted").is(false)
                .and("planId").is(planId);//.and("details.taskId").is(phaseId);

        if(feedbackType == VIVA||feedbackType == PPT){
            criteria.and("details.courseId").is(planTask.getPlan());
            if(feedbackType == VIVA) {
                List<String> phaseIds = planTask.getMilestones().stream().map(m->(String)m).toList();
               // criteria.and("phaseIds").elemMatch(new Criteria().and("phaseIds").in(phaseIds));
                criteria.and("phaseIds").size(phaseIds.size()).in(phaseIds);
            }
             //   criteria.and("phaseIds").is(planTask.getPlan());
        }
        else if(feedbackType == TEST){
            List<String> milestoneIds = planTask.getMilestones().stream().map(m->(String)m).toList();
            criteria.and("details.testId").is(planTask.getPlan());
            criteria.and("milestoneIds").size(milestoneIds.size()).in(milestoneIds);
            //criteria.and("milestoneIds").elemMatch(new Criteria().and("milestoneIds").in(milestoneIds));
        }
        Query query = new Query(criteria);
        List<Feedback_V2> feedbackList = mongoTemplate.find(query, Feedback_V2.class);
        List<CourseResponse_V2> courseResponseList = buildFeedbackResponseForCourseAndTest(feedbackList);
        return courseResponseList;
    }
    public List<CourseResponse_V2> findFeedbacksByCourseIdAndPhaseIdAndTraineeId(String planTaskId,String planId,String traineeId){
        PlanTask planTask = findMilestonesFromPlanTask(planTaskId);
        Criteria criteria = Criteria.where("traineeId").is(traineeId).and("type").is(VIVA_)
                .and("isDeleted").is(false)
                .and("details.courseId").is(planTask.getPlan())
                .and("planId").is(planId);//.and("details.taskId").is(phaseId);
        criteria.elemMatch(new Criteria().and("phaseIds").in(planTask.getMilestones()));
        Query query = new Query(criteria);
        List<Feedback_V2> feedbackList = mongoTemplate.find(query, Feedback_V2.class);

       // List<CourseResponse_V2> courseResponseList = buildFeedbackResponseForCourseAndTest(feedbackList);
       List<CourseResponse_V2> courseResponseList = buildFeedbackResponseForCourseAndTest(feedbackList);
        return courseResponseList;
    }

    public List<CourseResponse_V2> findFeedbacksByTestIdAndPMilestoneIdAndTraineeId(String testId,String planId,String traineeId){
        PlanTask planTask = findMilestonesFromPlanTask(testId);
        Criteria criteria = Criteria.where("traineeId").is(traineeId).and("type").is(TEST_)
                .and("isDeleted").is(false)
                .and("details.testId").is(testId)
                .and("planId").is(planId);
        criteria.elemMatch(new Criteria().and("milestoneIds").in(planTask.getMilestones()));
//        criteria.elemMatch(new Criteria().and("milestoneIds").in(milestoneIds));
        Query query = new Query(criteria);
        List<Feedback_V2> feedbackList = mongoTemplate.find(query,Feedback_V2.class);
        List<CourseResponse_V2> testResponseList = buildFeedbackResponseForCourseAndTest(feedbackList);
        return testResponseList;
    }

    public RatingReponseDto getOverallRatingOfTraineeForDashboard(String traineeId){
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("traineeId").is(traineeId).and("isDeleted").is(false)),
                Aggregation.group("type")
                        .sum("overallRating").as("rating")
                        .count().as("count"),
                Aggregation.project("count","rating","type")//.and("_id").as("type"),
        );
        AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(aggregation, "feedback_V2", Document.class);
        List<Document> documentList =  aggregationResults.getMappedResults();
        RatingReponseDto ratingReponseDto = RatingReponseDto.builder().build();

        for (Document d : documentList){
            if (TEST_.equals(d.get("_id")))
                ratingReponseDto.setTest(compute_rating((Double) d.get("rating"),(Integer) d.get("count")));
            else if (VIVA_.equals(d.get("_id")))
                ratingReponseDto.setCourse(compute_rating((Double) d.get("rating"),(Integer) d.get("count")));
            else if (PPT_.equals(d.get("_id")))
                ratingReponseDto.setPresentation(compute_rating((Double) d.get("rating"),(Integer) d.get("count")));
            else
                ratingReponseDto.setBehaviour(compute_rating((Double) d.get("rating"),(Integer) d.get("count")));
        };
        //float total = ratingReponseDto.getTest() + ratingReponseDto.getCourse() + ratingReponseDto.getPresentation() + ratingReponseDto.getBehaviour();
        ratingReponseDto.setOverall(computeOverallRatingOfTrainee(traineeId));
        ratingReponseDto.setComment(getFeedbackMessageBasedOnOverallRating(ratingReponseDto.getOverall()));
        return ratingReponseDto;
    }
    public List<FeedbackResponseDto> findFirstFiveFeedbacksOfTrainee(String traineeId){
        Criteria criteria = Criteria.where("traineeId").is(traineeId).and("isDeleted").is(false);
        Query query = new Query(criteria);
        query.with(Sort.by(Sort.Direction.DESC, "createdAt")).limit(5);
        List<Feedback_V2> feedbackV2List = mongoTemplate.find(query,Feedback_V2.class);
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        return feedbackV2List.stream().map(f -> FeedbackResponseDto.builder().feedback(f.getComment())
                .name(TrainingModuleApplication.searchNameById(f.getCreatedBy()))
                .date(formatter.format(f.getCreatedAt())).rating(compute_rating(f.getOverallRating(),1)).build()).toList();
    }

//    public DashboardResponse findFeedbacksSummaryOfTrainee(String traineeId){
//        Criteria criteria = Criteria.where("traineeId").is(traineeId);
//        Query query = new Query(criteria);
//        List<Feedback_V2> feedbackList = mongoTemplate.find(query,Feedback_V2.class);
//        float totalRating = 0;
//        int count = 0;
//        DashboardResponse dashboardResponse = DashboardResponse.builder().feedbacks(new ArrayList<>()).build();
//       // RatingReponseDto ratingReponseDto = RatingReponseDto.builder().build();
//        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
//        List<RatingDto> ratingDtoList = new ArrayList<>();
//        for (int i=0;i<4;i++) ratingDtoList.add(new RatingDto(0f,1));
//        for (Feedback_V2 feedback : feedbackList){
//            if(++count<5){
//                dashboardResponse.getFeedbacks().add(FeedbackResponseDto.builder().date(formatter.format(new Date()))
//                                .rating(feedback.getDetails().computeOverallRating())
//                                .feedback(feedback.getComment())
//                                .name(TrainingModuleApplication.searchNameById(feedback.getCreatedBy()))
//                        .build());
//            }
//            int type = feedback.getType().charAt(0) - '2';
//            ratingDtoList.get(type).incrTotalRating(feedback.getDetails().computeOverallRating());
//            ratingDtoList.get(type).incrcount();
//            totalRating += feedback.getDetails().computeOverallRating();
//        }
//        float overallRating = compute_rating(totalRating,feedbackList.size());
//        RatingReponseDto ratingReponseDto = RatingReponseDto.builder().overall(overallRating)
//                .course(compute_rating(ratingDtoList.get(1).getTotalRating(),ratingDtoList.get(1).getCount()))
//                .test(compute_rating(ratingDtoList.get(0).getTotalRating(),ratingDtoList.get(0).getCount()))
//                .presentation(compute_rating(ratingDtoList.get(2).getTotalRating(),ratingDtoList.get(2).getCount()))
//                .behaviour(compute_rating(ratingDtoList.get(3).getTotalRating(),ratingDtoList.get(3).getCount()))
//                .attendance(0f)
//                .comment(getFeedbackMessageBasedOnOverallRating(overallRating))
//                .build();
//
//        dashboardResponse.setRating(ratingReponseDto);
//        return dashboardResponse;
//        //return roundOff_Rating(totalRating/feedbackList.size());
//    }

    public static Double compute_rating(double totalRating,int count){
        if(totalRating==0) return 0.00;
        double temp = (int)(totalRating/count * 100);
//        return roundOff_Rating(totalRating/count);
        return  temp/100;
    }
    public static Float compute_rating1(double totalRating,int count){
        if(totalRating==0) return 0f;
        double num = totalRating/count;
        double truncatedNum = Math.floor(num * 100) / 100;

       // int temp = (int)(totalRating/count * 100);
//       return roundOff_Rating(totalRating/count);
        return (float)truncatedNum;
    }
    

    public Map<String,Object> computeOverallRating(String traineeId,String courseId,String planId,int type){
        if(courseId == null||planId == null) return null;
        Map<String,Object> response = new HashMap<>();
//        response.put("planRating",computeOverallRatingByTraineeIdAndTestIds(traineeId,criteriaList));
        response.put("planRating",computeOverallRatingOfTraineeOnPlan(traineeId,planId));
        response.put("overallRating",computeOverallRatingOfTrainee(traineeId));
        response.put("courseRating",computeRatingByTaskIdOfTrainee(traineeId,courseId, Integer.toString(type)));
        return response;
    }

    public Map<String,Double> computeOverallRating(String traineeId,String courseId,int type){
        if(courseId == null) return null;

        Criteria criteria = Criteria.where("userId").is(traineeId);//.and("deleted").is(false);
        Query query = new Query(criteria);
        System.out.println(traineeId + "////");
        AssignedPlan assignedPlan = mongoTemplate.findOne(query, AssignedPlan.class);
//        int tmp = (type == 1)?3:type;
        if (assignedPlan == null)
            throw new ApiException(HttpStatus.BAD_REQUEST,"No plan assigned!!");
        var plans =  assignedPlan.getPlans();
        AtomicReference<String> planId = new AtomicReference<>("");
        plans.forEach((p)->{
            System.out.println(p.getPhases().size() + "------>");
            var phases = p.getPhases();
            System.out.println(phases.size() + "------>");
            phases.forEach(ps -> {
                ps.getTasks().forEach(pt -> {
                    if (pt != null && pt instanceof PlanTask && pt.getPlanType() == type && pt.getPlan().equals(courseId))
                        planId.set(p.get_id());
                });
            });
        });

        Set<String> taskIds = new HashSet<>();
        Set<Criteria> criteriaList = new HashSet<>();
        plans.forEach((p)->{
            if(p.get_id().equals(planId.get())) {
                var phases = p.getPhases();
                phases.forEach(ps ->{
                    ps.getTasks().forEach(pt -> {
                        if (pt != null && pt instanceof PlanTask && pt.getPlanType()>=1 && pt.getPlanType()<=4){
                            String taskId = (pt.getPlanType().equals(TEST))?"testId":"courseId";
                            int ftype = (pt.getPlanType()==1)?3:pt.getPlanType();
                            criteriaList.add(Criteria.where("type").is(Integer.toString(ftype)).and(String.format("details.%s",taskId)).is(pt.getPlan()).and("traineeId").is(traineeId).and("isDeleted").is(false));
                        }
                    });
                });
            }
        });

        Map<String,Double> response = new HashMap<>();
        response.put("planRating",computeOverallRatingByTraineeIdAndTestIds(traineeId,criteriaList));
        response.put("overallRating",computeOverallRatingOfTrainee(traineeId));
        response.put("courseRating",computeRatingByTaskIdOfTrainee(traineeId,courseId, Integer.toString(type)));
        return response;
    }

//    Float computeOverallRatingForTrainee(){
//
//        Aggregation aggregation = newAggregation(
//                facet(
//                        // traineeOverAllRating facet
//                        asList(
//                                match(Criteria.where("traineeId").is("64e2e98aecc13d506c72c73a")),
//                                group().sum("overallRating").as("totalOverAllRating").count().as("count")
//                        ).as("traineeOverAllRating"),
//                        // allTestRating facet
//                        asList(
//                                match(new Criteria().orOperator(
//                                        Criteria.where("details.testId").in("65964a4c2c645a5bd0a8bb88", "65964fd70fc4521d6d5312c9", "6579b4500cf9d953fe39e4a6"),
//                                        Criteria.where("details.courseId").in("65964a4c2c645a5bd0a8bb88", "65964fd70fc4521d6d5312c9", "6579b4500cf9d953fe39e4a6")
//                                )),
//                                group().sum("overallRating").as("totalOverAllRating").count().as("count")
//                        ).as("allTestRating"),
//                        // testRating facet
//                        asList(
//                                match(Criteria.where("details.courseId").is("6579b4500cf9d953fe39e4a6")),
//                                group().sum("overallRating").as("totalOverAllRating").count().as("count")
//                        ).as("testRating")
//                )
//        );
//    }

    public Double computeOverallRatingOfTraineeOnPlan(String traineeId,String planId){
        Criteria criteria = Criteria.where("traineeId").is(traineeId).and("planId").is(planId).and("isDeleted").is(false);
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                group("planId")
                        .sum("overallRating").as("overallRating")
                        .count().as("count")
        );
        AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(aggregation, "feedback_V2", Document.class);
        Document document = aggregationResults.getUniqueMappedResult();
        if (document == null) return 0.00;
        int count = (int) document.get("count");
        double totalRating = (double) document.get("overallRating");
        return compute_rating(totalRating,count);
//        return roundOff_Rating(totalRating/count);
    }

    public Double computeOverallRatingByTraineeIdAndTestIds(String traineeId,Set<Criteria> taskIds){
        Criteria criteria = new Criteria().orOperator(taskIds);
        if (taskIds.size() == 0) return 0.00;
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                group("traineeId")
                        .sum("overallRating").as("overallRating")
                        .count().as("count")
        );
        AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(aggregation, "feedback_V2", Document.class);
        List<Document> document = aggregationResults.getMappedResults();
        if (document.isEmpty()) return 0.00;
        int count = (int) document.get(0).get("count");
        double totalRating = (double) document.get(0).get("overallRating");
        return compute_rating(totalRating,count);
//        return roundOff_Rating(totalRating/count);
    }

    public Double computeOverallRatingOfTrainee(String traineeId){
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("traineeId").is(traineeId).and("isDeleted").is(false)),
                group("traineeId")
                        .sum("overallRating").as("overallRating")
                        .count().as("count")
        );
        AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(aggregation, "feedback_V2", Document.class);
        List<Document> document = aggregationResults.getMappedResults();
        if (document.isEmpty()) return 0.00;
        int count = (int) document.get(0).get("count");
        double totalRating = (double) document.get(0).get("overallRating");
        return compute_rating(totalRating,count);
//        return roundOff_Rating(totalRating/count);
    }

    public Double computeRatingByTaskIdOfTrainee(String traineeId,String courseId,String type){
        Criteria criteria = Criteria.where("traineeId").is(traineeId).and("type").is(type)
                .and("isDeleted").is(false);
        if(type.equals(VIVA_)||type.equals(PPT_))
            criteria.and("details.courseId").is(courseId);
        else if (type.equals(TEST_))
            criteria.and("details.testId").is(courseId);

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                group("traineeId")
                        .sum("overallRating").as("overallRating")
                        .count().as("count")
        );
        AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(aggregation, "feedback_V2", Document.class);
        List<Document> document = aggregationResults.getMappedResults();
        if (document.isEmpty()) return 0.00;
        int count = (int) document.get(0).get("count");
        double totalRating = (double) document.get(0).get("overallRating");
        return compute_rating(totalRating,count);
//        return roundOff_Rating(totalRating/count);
    }

    public boolean courseExist(String traineeId,int tmp,String taskId){
        tmp = (tmp==1)?3:tmp;
        Integer type = tmp;
        Criteria criteria = Criteria.where("userId").is(traineeId);//.and("deleted").is(false);
        Query query = new Query(criteria);
        AssignedPlan assignedPlan = mongoTemplate.findOne(query, AssignedPlan.class);
        if (assignedPlan == null)
            throw new ApiException(HttpStatus.BAD_REQUEST,"No plan assigned!!");

        var plans =  assignedPlan.getPlans();
        List<String> planId = new ArrayList<>();
        plans.forEach((p)->{
            System.out.println(p.getPhases().size() + "------>");
            var phases = p.getPhases();
            System.out.println(phases.size() + "------>");
            phases.forEach(ps -> {
               boolean flag =  (ps.getEntityType() == type && ps.get_id().equals(taskId));
               if(flag) planId.add(p.get_id());
            });
        });
        if (planId.size() == 0)
            throw new ApiException(HttpStatus.BAD_REQUEST,"Task not assigned!!");
        return true;
    }

    public Map<String,Object> computeOverallRatingOfEmployee(String traineeId,String planId,String taskId,String subTaskId,String type){
//        Document document = new Document();
//        document.append("overallRating",Arrays.asList(
//                new Document("$match", new Document("traineeId", traineeId)).append("isDeleted",false),
//                new Document("$group", new Document("_id", null)
//                        .append("totalOverAllRating", new Document("$sum", "$overallRating"))
//                        .append("count", new Document("$sum", 1)))
//        ));
//        document.append("planRating",Arrays.asList(
//                new Document("$match", new Document("planId", planId)).append("isDeleted",false).append("traineeId",traineeId),
//                new Document("$group", new Document("_id", null)
//                        .append("totalOverAllRating", new Document("$sum", "$overallRating"))
//                        .append("count", new Document("$sum", 1)))
//        ));
//        String taskName = type.equals(TEST_)?"details.testId":"details.courseId";
//
//        document.append("courseRating",Arrays.asList(
//                new Document("$match", new Document("planId", planId).append(taskName,taskId)),
//                new Document("$group", new Document("_id", null)
//                        .append("totalOverAllRating", new Document("$sum", "$overallRating"))
//                        .append("count", new Document("$sum", 1)))
//        ));
        Document document = new Document();
        document.append("overallRating",Arrays.asList(
                new Document("$match", new Document("traineeId", traineeId).append("isDeleted",false)),
                new Document("$group", new Document("_id", null)
                        .append("totalOverAllRating", new Document("$sum", "$overallRating"))
                        .append("count", new Document("$sum", 1)))
        ));
        document.append("planRating",Arrays.asList(
                new Document("$match", new Document("planId", planId).append("isDeleted",false).append("traineeId",traineeId)),
                new Document("$group", new Document("_id", null)
                        .append("totalOverAllRating", new Document("$sum", "$overallRating"))
                        .append("count", new Document("$sum", 1)))
        ));
        //String taskName = type.equals(TEST_)?"details.testId":"details.courseId";
        if(type.equals(TEST_)||type.equals(VIVA_)) {
            Criteria criteria1 = Criteria.where("_id").is(subTaskId);
            Query query = new Query(criteria1);
            PlanTask planTask = mongoTemplate.findOne(query, PlanTask.class);
            if(planTask == null) throw new ApiException(HttpStatus.BAD_REQUEST,"Invalid plan task Id!!");
            List<Object> milestoneObjectIds = planTask.getMilestones();
            if(milestoneObjectIds == null || milestoneObjectIds.isEmpty()) throw new ApiException(HttpStatus.BAD_REQUEST,"No tasks available in this milestone!!");
            List<String> milestoneIds = new ArrayList<>();
            milestoneObjectIds.forEach((mo)->milestoneIds.add((String) mo));
            String taskName = type.equals(TEST_)?"details.testId":"details.courseId";
            String subTaskName = type.equals(TEST_)?"milestoneIds":"phaseIds";

            document.append("courseRating", Arrays.asList(
                    new Document("$match", new Document("planId", planId).append(taskName, taskId).append("isDeleted", false).append("traineeId", traineeId).append(subTaskName,new Document("$in",milestoneIds))),
                    new Document("$group", new Document("_id", null)
                            .append("totalOverAllRating", new Document("$sum", "$overallRating"))
                            .append("count", new Document("$sum", 1)))
            ));
        }
         else if(type.equals(PPT_)){
            document.append("courseRating", Arrays.asList(
                    new Document("$match", new Document("planId", planId).append("details.courseId", taskId).append("isDeleted", false).append("traineeId", traineeId)),
                    new Document("$group", new Document("_id", null)
                            .append("totalOverAllRating", new Document("$sum", "$overallRating"))
                            .append("count", new Document("$sum", 1)))
            ));
        }
        Aggregation aggregation = newAggregation(
                context -> new Document("$facet", document)
        );
        Document results = mongoTemplate.aggregate(
                aggregation, "feedback_V2", Document.class
        ).getUniqueMappedResult();
        Map<String, Object> response = new HashMap<String, Object>();
        List<Document> traineeOverAllRating = (List<Document>) results.get("overallRating");
        if(traineeOverAllRating == null || traineeOverAllRating.isEmpty()) {
            response.put("overallRating", 0f);
        }else{
            Double totalOverAllRating = (Double) traineeOverAllRating.get(0).get("totalOverAllRating");
            Integer count = (Integer) traineeOverAllRating.get(0).get("count");
            response.put("overallRating", compute_rating(totalOverAllRating,count));
        }
        List<Document> planRating = (List<Document>) results.get("planRating");
        if (planRating == null || planRating.isEmpty()) {
            response.put("planRating", 0f);
        }else{
            Double totalOverAllRating = (Double) planRating.get(0).get("totalOverAllRating");
            Integer count = (Integer) planRating.get(0).get("count");
            response.put("planRating", compute_rating(totalOverAllRating,count));
        }
        List<Document> courseRating = (List<Document>) results.get("courseRating");
        if (courseRating == null || courseRating.isEmpty()) {
            response.put("courseRating", 0f);
        }else{
            Double totalOverAllRating = (Double) courseRating.get(0).get("totalOverAllRating");
            Integer count = (Integer) courseRating.get(0).get("count");
            response.put("courseRating", compute_rating(totalOverAllRating,count));
        }

//        response.put("traineeOverAllRating", results.get("traineeOverAllRating")==null?0:compute_rating((Double) results.get("traineeOverAllRating.totalOverAllRating"),(Integer) results.get("traineeOverAllRating.count")));
//        response.put("planRating",results.get("planRating")==null?0:compute_rating((Double) results.get("planRating.totalOverAllRating"),(Integer) results.get("planRating.count")));
//        response.put("courseRating",results.get("courseRating")==null?0:compute_rating((Double) results.get("courseRating.totalOverAllRating"),(Integer) results.get("courseRating.count")));
        return response;
    }
    public PlanTask findMilestonesFromPlanTask(String _id){
        return mongoTemplate.findById(_id,PlanTask.class);
    }
    public void testAggregationQuery(String _id){
        Criteria criteria = Criteria.where("traineeId").is("64e2e98aecc13d506c72c73a").and("isDeleted").is(false);
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                group("createdBy")
                        .sum("overallRating").as("overallRating")
                        .count().as("count")
                        .push("$$ROOT").as("feedbacks")
//                        .first("createdBy").as("createdBy")
        );
        List<Document> documents = mongoTemplate.aggregate(aggregation,"feedback_V2",Document.class).getMappedResults();
        for (Document document : documents){
            List<Feedback_V2> feedback_v2List = (List<Feedback_V2>) document.get("feedbacks");
            System.out.println(feedback_v2List.size() + "//");
        }
        System.out.println(documents.size() + "///");
    }

    public void deleteFeedbacksGivenOnTraineePlans(String traineeId,List<String> planIds){
        Criteria criteria = Criteria.where("traineeId").is(traineeId).and("planId").in(planIds);
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");

        Update update = new Update().set("isDeleted",true)
                .set("updateAt",date);
        mongoTemplate.updateMulti(new Query(criteria),update,Feedback_V2.class);
    }

}
