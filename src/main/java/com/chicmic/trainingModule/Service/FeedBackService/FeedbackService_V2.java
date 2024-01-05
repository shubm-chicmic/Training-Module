package com.chicmic.trainingModule.Service.FeedBackService;

import com.chicmic.trainingModule.Dto.*;
import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponse;
import com.chicmic.trainingModule.Dto.CourseResponse_V2.CourseResponse_V2;
import com.chicmic.trainingModule.Dto.DashboardDto.DashboardResponse;
import com.chicmic.trainingModule.Dto.DashboardDto.FeedbackResponseDto;
import com.chicmic.trainingModule.Dto.DashboardDto.RatingDto;
import com.chicmic.trainingModule.Dto.DashboardDto.RatingReponseDto;
import com.chicmic.trainingModule.Dto.FeedbackDto.FeedbackRequestDto;
import com.chicmic.trainingModule.Dto.FeedbackDto.RatingAndCountDto;
import com.chicmic.trainingModule.Dto.FeedbackDto.TaskIdAndTypeDto;
import com.chicmic.trainingModule.Dto.FeedbackResponseDto_V2.FeedbackResponse;
import com.chicmic.trainingModule.Dto.PhaseResponse_V2.PhaseResponse_V2;
import com.chicmic.trainingModule.Dto.rating.Rating;
import com.chicmic.trainingModule.Dto.rating.Rating_COURSE;
import com.chicmic.trainingModule.Dto.rating.Rating_PPT;
import com.chicmic.trainingModule.Dto.rating.Rating_TEST;
import com.chicmic.trainingModule.Entity.AssignedPlan;
import com.chicmic.trainingModule.Entity.Feedback_V2;
import com.chicmic.trainingModule.Entity.Phase;
import com.chicmic.trainingModule.Entity.Plan;
import com.chicmic.trainingModule.ExceptionHandling.ApiException;
import com.chicmic.trainingModule.Service.CourseServices.CourseService;
import com.chicmic.trainingModule.Service.TestServices.TestService;
import com.chicmic.trainingModule.TrainingModuleApplication;
import com.chicmic.trainingModule.Util.FeedbackUtil;
import org.bson.Document;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static com.chicmic.trainingModule.Dto.FeedbackResponseDto_V2.FeedbackResponse.buildFeedbackResponse;
import static com.chicmic.trainingModule.Entity.Constants.EntityType.COURSE;
import static com.chicmic.trainingModule.Entity.Feedback_V2.buildFeedbackFromFeedbackRequestDto;
import static com.chicmic.trainingModule.TrainingModuleApplication.idUserMap;
import static com.chicmic.trainingModule.Util.FeedbackUtil.*;
import static com.chicmic.trainingModule.Util.RatingUtil.roundOff_Rating;
import static com.chicmic.trainingModule.Util.TrimNullValidator.FeedbackType.*;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;

@Service
public class FeedbackService_V2 {
    private final MongoTemplate mongoTemplate;
    private final TestService testService;
    private final CourseService courseService;

    public FeedbackService_V2(MongoTemplate mongoTemplate, TestService testService, CourseService courseService) {
        this.mongoTemplate = mongoTemplate;
        this.testService = testService;
        this.courseService = courseService;
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
        if(feedback_v2.getMilestoneIds() != null)
                criteria.and("details.testId").is(testId);

        if (feedback_v2.getPhaseIds() != null)
            criteria.and("phaseIds").is(feedback_v2.getPhaseIds());
        if (feedback_v2.getMilestoneIds() != null)
            criteria.and("milestoneIds").is(feedback_v2.getMilestoneIds());

        return mongoTemplate.exists(new Query(criteria), Feedback_V2.class);
    }

    public FeedbackResponse saveFeedbackInDb(FeedbackRequestDto feedbackDto, String reviewerId){
        //checking traineeId is valid
        TrainingModuleApplication.searchUserById(feedbackDto.getTrainee());

        Feedback_V2 feedback = buildFeedbackFromFeedbackRequestDto(feedbackDto,reviewerId);
        //course assigned or not!!

        //course assigned to a trainee or not!!
//        if (feedbackDto.getFeedbackType().equals("1")||feedbackDto.getFeedbackType().equals("2"))
//            courseExist(feedbackDto.getTrainee(),feedbackDto.getFeedbackType().charAt(0) - '0',feedback.getDetails().getTaskId());

        //checking feedback exist or not!!
        boolean flag = feedbackExist(feedback,reviewerId);
        if (flag) throw new ApiException(HttpStatus.BAD_REQUEST,"Feedback submitted previously!!");

        Feedback_V2 feedbackV2 =  mongoTemplate.insert(feedback,"feedback_V2");
        FeedbackResponse feedbackResponse = buildFeedbackResponse(feedbackV2);
        addTaskNameAndSubTaskName(Arrays.asList(feedbackResponse));
        return feedbackResponse;
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

        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");


        Rating rating = Rating.getRating(feedbackRequestDto);
        Update update = new Update()
                .set("updateAt",formatter.format(date))
                .set("details",rating)
                .set("comment",feedbackRequestDto.getComment())
                .set("overallRating",feedbackRequestDto.computeRating());

       FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true);

        Feedback_V2 feedback_v2 = mongoTemplate.findAndModify(new Query(criteria),update,options,Feedback_V2.class);
        FeedbackResponse feedbackResponse = buildFeedbackResponse(feedback_v2);
        addTaskNameAndSubTaskName(Arrays.asList(feedbackResponse));
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

        FeedbackResponse_V2 feedbackResponseV2 = FeedbackResponse_V2.buildResponse(feedbackV2);
       // addTaskNameAndSubTaskName(Arrays.asList(feedbackResponseV2))
        addTaskNameAndSubTaskName(feedbackResponseV2);
        return feedbackResponseV2;
    }
    public ApiResponse findFeedbacksOnUserPlan(String traineeId,String planId,Integer pageNumber, Integer pageSize){
        List<Criteria> criteriaList = getAllTaskIdsInPlan(traineeId,planId);
        List<Document> userDatasDocuments = idUserMap.values().stream().map(userDto ->
                        new Document("reviewerName",userDto.getName()).append("reviewerTeam",userDto.getTeamName()).append("reviewerCode",userDto.getEmpCode())
                                .append("id",userDto.get_id()))
                .toList();

//        Criteria criteria = Criteria.where("traineeId").is(traineeId)
//                .and("isDeleted").is(false);
        Criteria criteria = new Criteria().orOperator(criteriaList);
        int skipValue = (pageNumber - 1) * pageSize;

        Aggregation aggregation = newAggregation(
                match(criteria),
                context -> new Document("$addFields", new Document("userDatas", userDatasDocuments)),
                context -> new Document("$addFields", new Document("userData",
                        new Document("$filter",
                                new Document("input", "$userDatas")
                                        .append("as", "user")
                                        .append("cond", new Document("$eq", Arrays.asList("$$user.id", "$createdBy")))
                        )
                )),
                context -> new Document("$unwind",
                        new Document("path", "$userData")
                                .append("preserveNullAndEmptyArrays", true)
                ),
                context -> new Document("$project", new Document("userDatas", 0)),
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
                       criteriaList.add(Criteria.where("type").is(tk.getPlanType().toString()).and(taskId).is(tk.getPlan()).and("traineeId").is(traineeId).and("isDeleted").is(false));
                   }
               });
            }
        });
        return criteriaList;
    }

    public ApiResponse findTraineeFeedbacks(Integer pageNumber, Integer pageSize, String query, Integer sortDirection, String sortKey, String traineeId) {
        List<Document> userDatasDocuments = idUserMap.values().stream().map(userDto ->
                        new Document("reviewerName",userDto.getName()).append("reviewerTeam",userDto.getTeamName()).append("reviewerCode",userDto.getEmpCode())
                                .append("id",userDto.get_id()))
                .toList();

        Criteria criteria = Criteria.where("traineeId").is(traineeId)
                .and("isDeleted").is(false);
//
//        //searching!!!
        if(query==null || query.isBlank()) query = ".*";
        int skipValue = (pageNumber - 1) * pageSize;
        java.util.regex.Pattern namePattern = java.util.regex.Pattern.compile(query, java.util.regex.Pattern.CASE_INSENSITIVE);
        Aggregation aggregation = newAggregation(
                match(criteria),
                context -> new Document("$addFields", new Document("userDatas", userDatasDocuments)),
                context -> new Document("$addFields", new Document("userData",
                        new Document("$filter",
                                new Document("input", "$userDatas")
                                        .append("as", "user")
                                        .append("cond", new Document("$eq", Arrays.asList("$$user.id", "$createdBy")))
                        )
                )),
                context -> new Document("$unwind",
                        new Document("path", "$userData")
                                .append("preserveNullAndEmptyArrays", true)
                ),
                context -> new Document("$project", new Document("userDatas", 0)),
                context -> new Document("$match", new Document("$or", Arrays.asList(
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
        for (Feedback_V2 feedbackV2 : feedbackList)
            feedbackResponse_v2List.add(buildFeedbackResponse(feedbackV2));

        addTaskNameAndSubTaskName(feedbackResponse_v2List);
        long count = mongoTemplate.count(new Query(criteria),Feedback_V2.class);
        return new ApiResponse(200,"List of All feedbacks",feedbackResponse_v2List,count);
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
        int skipValue = (pageNumber - 1) * pageSize;

//        System.out.println(userDatasDocuments.size() + "///");
        java.util.regex.Pattern namePattern = java.util.regex.Pattern.compile(query, java.util.regex.Pattern.CASE_INSENSITIVE);
        Aggregation aggregation = newAggregation(
                match(criteria),
                context -> new Document("$addFields", new Document("userDatas", userDatasDocuments)),
                context -> new Document("$addFields", new Document("userData",
                        new Document("$filter",
                                new Document("input", "$userDatas")
                                        .append("as", "user")
                                        .append("cond", new Document("$eq", Arrays.asList("$$user.id", "$traineeId")))
                        )
                )),
                context -> new Document("$unwind",
                        new Document("path", "$userData")
                                .append("preserveNullAndEmptyArrays", true)
                ),
                context -> new Document("$project", new Document("userDatas", 0)),
                context -> new Document("$match", new Document("$or", Arrays.asList(
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
    public void addSubTaskName(List<CourseResponse_V2> courseResponseList,int type){
        List<String> courseIds = new ArrayList<>();
        List<String> testIds = new ArrayList<>();
        courseResponseList.forEach(f ->{
//            if (f.getFeedbackType().get_id().equals(VIVA_) || f.getFeedbackType().get_id().equals(PPT_))
//                courseIds.add(f.getTask().get_id());
//            else if (f.getFeedbackType().get_id().equals(TEST_)){
//                testIds.add(f.getTask().get_id());
//            }
        });
        return;
    }
    public void addTaskNameAndSubTaskName(List<FeedbackResponse> feedbackResponseList){
        List<String> courseIds = new ArrayList<>();
        List<String> testIds = new ArrayList<>();
        feedbackResponseList.forEach(f ->{
            if (f.getFeedbackType().get_id().equals(VIVA_) || f.getFeedbackType().get_id().equals(PPT_))
                courseIds.add(f.getTask().get_id());
            else if (f.getFeedbackType().get_id().equals(TEST_)){
                testIds.add(f.getTask().get_id());
            }
        });
        var testDetails = testService.findTestsByIds(testIds);
        var courseDetails = courseService.findCoursesByIds(courseIds);

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
        List<String> courseIds = new ArrayList<>();
        List<String> testIds = new ArrayList<>();
        feedbackList.forEach(f -> {
            if(f.getType().equals("COURSE")||f.getType().equals("PPT"))
                courseIds.add(f.getDetails().getCourseId());
            else if(f.getType().equals("TEST"))
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
                        .overallRating(5.0f)
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
            courseResponseList.get(index).setOverallRating(roundOff_Rating(rating.getRating()/rating.getCount()));
        }
        //courseResponseList = addSubTaskName(courseResponseList,);
        return courseResponseList;
    }

    public PhaseResponse_V2 buildPhaseResponseForCourseOrTest(Feedback_V2 feedback_v2,Map<String,String> phaseDetails,Map<String,String> milestoneDetails){
        PhaseResponse_V2 phaseResponse = PhaseResponse_V2.builder()
                .comment(feedback_v2.getComment())
                .overallRating(feedback_v2.getDetails().computeOverallRating())
                .createdAt(feedback_v2.getCreatedAt())
                .subTask(new ArrayList<>())
                .build();
        if(feedback_v2.getType().equals(VIVA_)) {
            Rating_COURSE ratingCourse = (Rating_COURSE) feedback_v2.getDetails();
            feedback_v2.getPhaseIds().forEach(st -> { phaseResponse.getSubTask().add(new UserIdAndNameDto(st, phaseDetails.get(st)));});
//            phaseResponse.set_id(ratingCourse.getPhaseId());
//            phaseResponse.setName(name.get(ratingCourse.getPhaseId()));
            phaseResponse.setTheoreticalRating(ratingCourse.getTheoreticalRating());
            phaseResponse.setCommunicationRating(ratingCourse.getCommunicationRating());
            phaseResponse.setTechnicalRating(ratingCourse.getTechnicalRating());
        }else if(feedback_v2.getType().equals(TEST_)){
            Rating_TEST ratingTest = (Rating_TEST) feedback_v2.getDetails();
            feedback_v2.getMilestoneIds().forEach(st -> { phaseResponse.getSubTask().add(new UserIdAndNameDto(st,milestoneDetails.get(st)));});
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
                Aggregation.group("traineeId")
                        .sum("overallRating").as("overallRating")
                        .count().as("count")
        );
        AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(aggregation, "feedback_V2", Document.class);
        return aggregationResults.getMappedResults();
    }

    public List<CourseResponse_V2> findFeedbacksByCourseIdAndPhaseIdAndTraineeId(String courseId,String phaseId,String traineeId){
        Criteria criteria = Criteria.where("traineeId").is(traineeId).and("type").is("COURSE")
                .and("isDeleted").is(false)
                .and("details.taskId").is(courseId);//.and("details.taskId").is(phaseId);
        criteria.elemMatch(new Criteria().and("subtaskIds").in(phaseId));
        Query query = new Query(criteria);
        List<Feedback_V2> feedbackList = mongoTemplate.find(query, Feedback_V2.class);

       // List<CourseResponse_V2> courseResponseList = buildFeedbackResponseForCourseAndTest(feedbackList);
       List<CourseResponse_V2> courseResponseList = buildFeedbackResponseForCourseAndTest(feedbackList);
        return null;
    }
    public List<CourseResponse_V2> findFeedbacksByTestIdAndPMilestoneIdAndTraineeId(String testId,String milestoneid,String traineeId){
        Criteria criteria = Criteria.where("traineeId").is(traineeId).and("type").is("TEST")
                .and("isDeleted").is(false)
                .and("details.taskId").is(testId);
        criteria.elemMatch(new Criteria().and("subtaskIds").in(milestoneid));
        Query query = new Query(criteria);
        List<Feedback_V2> feedbackList = mongoTemplate.find(query,Feedback_V2.class);
        List<CourseResponse_V2> testResponseList = buildFeedbackResponseForCourseAndTest(feedbackList);
        return testResponseList;
    }
    public DashboardResponse findFeedbacksSummaryOfTrainee(String traineeId){
        Criteria criteria = Criteria.where("traineeId").is(traineeId);
        Query query = new Query(criteria);
        List<Feedback_V2> feedbackList = mongoTemplate.find(query,Feedback_V2.class);
        float totalRating = 0;
        int count = 0;
        DashboardResponse dashboardResponse = DashboardResponse.builder().feedbacks(new ArrayList<>()).build();
       // RatingReponseDto ratingReponseDto = RatingReponseDto.builder().build();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        List<RatingDto> ratingDtoList = new ArrayList<>();
        for (int i=0;i<4;i++) ratingDtoList.add(new RatingDto(0f,1));
        for (Feedback_V2 feedback : feedbackList){
            if(++count<5){
                dashboardResponse.getFeedbacks().add(FeedbackResponseDto.builder().date(formatter.format(new Date()))
                                .rating(feedback.getDetails().computeOverallRating())
                                .feedback(feedback.getComment())
                                .name(TrainingModuleApplication.searchNameById(feedback.getCreatedBy()))
                        .build());
            }
            int type = feedback.getType().charAt(0) - '2';
            ratingDtoList.get(type).incrTotalRating(feedback.getDetails().computeOverallRating());
            ratingDtoList.get(type).incrcount();
            totalRating += feedback.getDetails().computeOverallRating();
        }
        float overallRating = compute_rating(totalRating,feedbackList.size());
        RatingReponseDto ratingReponseDto = RatingReponseDto.builder().overall(overallRating)
                .course(compute_rating(ratingDtoList.get(1).getTotalRating(),ratingDtoList.get(1).getCount()))
                .test(compute_rating(ratingDtoList.get(0).getTotalRating(),ratingDtoList.get(0).getCount()))
                .presentation(compute_rating(ratingDtoList.get(2).getTotalRating(),ratingDtoList.get(2).getCount()))
                .behaviour(compute_rating(ratingDtoList.get(3).getTotalRating(),ratingDtoList.get(3).getCount()))
                .attendance(0f)
                .comment(getFeedbackMessageBasedOnOverallRating(overallRating))
                .build();

        dashboardResponse.setRating(ratingReponseDto);
        return dashboardResponse;
        //return roundOff_Rating(totalRating/feedbackList.size());
    }

    private static Float compute_rating(float totalRating,int count){
        if(totalRating==0) return 0f;
        int temp = (int)(totalRating/count * 100);
//        return roundOff_Rating(totalRating/count);
        return ((float) temp) / 100;
    }
    public Map<String,Float> computeOverallRating(String traineeId,String courseId,int type){

        Criteria criteria = Criteria.where("userId").is(traineeId);//.and("deleted").is(false);
        Query query = new Query(criteria);
        System.out.println(traineeId + "////");
        AssignedPlan assignedPlan = mongoTemplate.findOne(query, AssignedPlan.class);
        int tmp = (type == 1)?3:type;
        if (assignedPlan == null)
            throw new ApiException(HttpStatus.BAD_REQUEST,"No plan assigned!!");
        var plans =  assignedPlan.getPlans();
        AtomicReference<String> planId = new AtomicReference<>("");
        plans.forEach((p)->{
            System.out.println(p.getPhases().size() + "------>");
            var phases = p.getPhases();
            System.out.println(phases.size() + "------>");
            phases.forEach(ps -> {
                System.out.println(ps.get_id() + "////");
                if (ps.getEntityType() == tmp && ps.get_id().equals(courseId))
                    planId.set(p.get_id());
            });
        });

        Set<String> taskIds = new HashSet<>();
        plans.forEach((p)->{
            if(p.get_id().equals(planId.get())) {
                var phases = p.getPhases();
                phases.forEach(ps -> taskIds.add(ps.get_id()));
            }
        });
        Map<String,Float> response = new HashMap<>();
        response.put("planRating",computeOverallRatingByTraineeIdAndTestIds(traineeId,taskIds));
        response.put("overallRating",computeOverallRatingOfTrainee(traineeId));
        response.put("courseRating",computeRatingByTaskIdOfTrainee(traineeId,courseId, FEEDBACK_TYPE_CATEGORY_V2[type-1]));
        return response;
    }
    Float computeOverallRatingByTraineeIdAndTestIds(String traineeId,Set<String> taskIds){
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("traineeId").is(traineeId).and("isDeleted").is(false).and("details.taskId").in(taskIds)),
                Aggregation.group("traineeId")
                        .sum("overallRating").as("overallRating")
                        .count().as("count")
        );
        AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(aggregation, "feedback_V2", Document.class);
        List<Document> document = aggregationResults.getMappedResults();
        if (document.isEmpty()) return 0f;
        int count = (int) document.get(0).get("count");
        double totalRating = (double) document.get(0).get("overallRating");
        return roundOff_Rating(totalRating/count);
    }

    Float computeOverallRatingOfTrainee(String traineeId){
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("traineeId").is(traineeId).and("isDeleted").is(false)),
                Aggregation.group("traineeId")
                        .sum("overallRating").as("overallRating")
                        .count().as("count")
        );
        AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(aggregation, "feedback_V2", Document.class);
        List<Document> document = aggregationResults.getMappedResults();
        if (document.isEmpty()) return 0f;
        int count = (int) document.get(0).get("count");
        double totalRating = (double) document.get(0).get("overallRating");
        return roundOff_Rating(totalRating/count);
    }
    public Float computeRatingByTaskIdOfTrainee(String traineeId, String courseId, String type){
        Criteria criteria = Criteria.where("traineeId").is(traineeId).and("type").is(type)
                .and("isDeleted").is(false);
        if(type.equals(VIVA_)||type.equals(PPT_))
            criteria.and("details.courseId").is(courseId);
        else if (type.equals(TEST_))
            criteria.and("details.testId").is(courseId);
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("traineeId")
                        .sum("overallRating").as("overallRating")
                        .count().as("count")
        );
        AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(aggregation, "feedback_V2", Document.class);
        List<Document> document = aggregationResults.getMappedResults();
        if (document.isEmpty()) return 0f;
        int count = (int) document.get(0).get("count");
        double totalRating = (double) document.get(0).get("overallRating");
        return roundOff_Rating(totalRating/count);
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
}
