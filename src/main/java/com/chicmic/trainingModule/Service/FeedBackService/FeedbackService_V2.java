package com.chicmic.trainingModule.Service.FeedBackService;

import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponse;
import com.chicmic.trainingModule.Dto.CourseResponse_V2.CourseResponse_V2;
import com.chicmic.trainingModule.Dto.FeedbackDto.FeedbackRequestDto;
import com.chicmic.trainingModule.Dto.FeedbackResponseDto_V2.FeedbackResponse;
import com.chicmic.trainingModule.Dto.FeedbackResponse_V2;
import com.chicmic.trainingModule.Dto.rating.Rating;
import com.chicmic.trainingModule.Entity.Feedback_V2;
import com.chicmic.trainingModule.ExceptionHandling.ApiException;
import com.chicmic.trainingModule.Service.CourseServices.CourseService;
import com.chicmic.trainingModule.Service.TestServices.TestService;
import com.chicmic.trainingModule.TrainingModuleApplication;
import org.bson.Document;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.chicmic.trainingModule.Dto.FeedbackResponseDto_V2.FeedbackResponse.buildFeedbackResponse;
import static com.chicmic.trainingModule.Entity.Feedback_V2.buildFeedbackFromFeedbackRequestDto;
import static com.chicmic.trainingModule.TrainingModuleApplication.idUserMap;
import static com.chicmic.trainingModule.Util.FeedbackUtil.FEEDBACK_TYPE_CATEGORY_V2;
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
        String taskId = feedback_v2.getDetails().getTaskId();
        if(taskId != null)
            criteria.and("details.taskId").is(taskId);
        if(feedback_v2.getSubtaskIds() != null)
                criteria.and("subtaskIds").is(feedback_v2.getSubtaskIds());
        return mongoTemplate.exists(new Query(criteria), Feedback_V2.class);
    }

    public FeedbackResponse saveFeedbackInDb(FeedbackRequestDto feedbackDto, String reviewerId){
        //checking traineeId is valid
        TrainingModuleApplication.searchUserById(feedbackDto.getTrainee());

        Feedback_V2 feedback = buildFeedbackFromFeedbackRequestDto(feedbackDto,reviewerId);
        //course assigned or not!!

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
        String type = FEEDBACK_TYPE_CATEGORY_V2[feedbackRequestDto.getFeedbackType().charAt(0) - '1'];

        Criteria criteria = Criteria.where("_id").is(feedbackRequestDto.get_id())
                .and("createdBy").is(reviewerId).and("type").is(type);

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
                .set("comment",feedbackRequestDto.getComment());

       FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true);

        Feedback_V2 feedback_v2 = mongoTemplate.findAndModify(new Query(criteria),update,options,Feedback_V2.class);
        FeedbackResponse feedbackResponse = buildFeedbackResponse(feedback_v2);
        addTaskNameAndSubTaskName(Arrays.asList(feedbackResponse));
        return feedbackResponse;
    }
    public String deleteFeedbackById(String _id,String reviewerId){
        Criteria criteria = Criteria.where("_id").is(_id).and("createdBy").is(reviewerId);
        Query query = new Query(criteria);
        Update update = new Update().set("isDeleted",true);
        Feedback_V2 feedback = mongoTemplate.findAndModify(query,update,Feedback_V2.class);
        if(feedback == null)  throw new ApiException(HttpStatus.valueOf(400),"You can't delete this feedback");

        return feedback.getTraineeId();
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

    public ApiResponse findTraineeFeedbacks(Integer pageNumber, Integer pageSize, String query, Integer sortDirection, String sortKey, String traineeId) {
        List<Document> userDatasDocuments = idUserMap.values().stream().map(userDto ->
                        new Document("reviewerName",userDto.getName()).append("reviewerTeam",userDto.getTeamName()).append("reviewerCode",userDto.getEmpCode())
                                .append("id",userDto.get_id()))
                .toList();

        Criteria criteria = Criteria.where("traineeID").is(traineeId)
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

    public void addTaskNameAndSubTaskName(List<FeedbackResponse> feedbackResponseList){
        List<String> courseIds = new ArrayList<>();
        List<String> testIds = new ArrayList<>();
        feedbackResponseList.forEach(f ->{
            if (f.getFeedbackType().get_id().equals("1") || f.getFeedbackType().get_id().equals("4"))
                courseIds.add(f.getTask().get_id());
            else if (f.getFeedbackType().get_id().equals("2")){
                testIds.add(f.getTask().get_id());
            }
        });
        var testDetails = testService.findTestsByIds(testIds);
        var courseDetails = courseService.findCoursesByIds(courseIds);

        feedbackResponseList.forEach(f->{
            if (f.getFeedbackType().get_id().equals("1") || f.getFeedbackType().get_id().equals("4")){
                f.getTask().setName(courseDetails.get(0).get(f.getTask().get_id()));
                //adding phaseName
                f.getSubTask().forEach( p -> p.setName(courseDetails.get(1).get(p.get_id())));
            }
            else if (f.getFeedbackType().get_id().equals("2"))
                f.getTask().setName(testDetails.get(0).get(f.getTask().get_id()));
                //adding milestone name
            f.getSubTask().forEach(m -> m.setName(testDetails.get(1).get(m.get_id())));
        });
    }
    public void addTaskNameAndSubTaskName(FeedbackResponse_V2 f){
        List<String> courseIds = new ArrayList<>();
        List<String> testIds = new ArrayList<>();
        if (f.getFeedbackType() == 1 || f.getFeedbackType() == 4)
            courseIds.add(f.getCourse().get_id());
        else if (f.getFeedbackType() == 2){
            testIds.add(f.getTest().get_id());
        }
        var testDetails = testService.findTestsByIds(testIds);
        var courseDetails = courseService.findCoursesByIds(courseIds);

        if (f.getFeedbackType() == 1 || f.getFeedbackType() == 4) {
            f.getCourse().setName(courseDetails.get(0).get(f.getCourse().get_id()));
            if (f.getFeedbackType() == 1) f.getPhase().forEach(p -> p.setName(courseDetails.get(1).get(p.get_id())));
        }
        else if (f.getFeedbackType() == 2){
            f.getTest().setName(testDetails.get(0).get(f.getTest().get_id()));
            f.getMilestone().forEach(m -> m.setName(testDetails.get(1).get(m.get_id())));
        }
    }
    public List<Feedback_V2> findFeedbacksByTaskIdAndTraineeIdAndType(String _id,String traineeId,String type){
        Criteria criteria = Criteria.where("type").is(type).and("traineeId").is(traineeId).and("details.taskId")
                .is(_id);
        Query query = new Query(criteria);
        return mongoTemplate.find(query,Feedback_V2.class);
    }
    public List<CourseResponse_V2> buildFeedbackResponseForCourseAndTest(List<Feedback_V2> feedbackList, String _id, Integer type){
//        Map<String,String> names = new HashMap<>();
//        if(type == 1) names = getPhaseName(_id);
//        else if (type == 2) names = getTestName(_id);
//        else if(type == 3) names = getCoursesName(feedbackList);


//        HashMap<String,TraineeRating> dp = new HashMap<>();
//        List<Reviewer> courseResponseList = new ArrayList<>();
//        List<CourseResponse> courseResponseList = new ArrayList<>();
//        for(Feedback feedback : feedbackList){
//            String reviewerId = feedback.getCreatedBy();
//            if(dp.get(reviewerId) == null) {//not present
//                UserDto reviewer = TrainingModuleApplication.searchUserById(reviewerId);
//
//                //creating a new element for reviewer
//                CourseResponse courseResponse = CourseResponse.builder()
//                        ._id(reviewerId)
//                        .reviewerName(reviewer.getName())
//                        .code(reviewer.getEmpCode())
//                        .overallRating(5.0f)
//                        .records(new ArrayList<>())
//                        .build();
//
//                PhaseResponse phaseResponse = buildPhaseResponseForCourseOrTest(feedback,names);
//                //adding the phase into it
//                courseResponse.getRecords().add(phaseResponse);
//                dp.put(reviewerId,new TraineeRating(courseResponseList.size(),feedback.getOverallRating(),1));
//                courseResponseList.add(new Reviewer(courseResponse));
//                courseResponseList.add(courseResponse);
//            }else{
//                //int idx = dp.get(reviewerId).getIndex();
//                TraineeRating traineeRating = dp.get(reviewerId);
//                PhaseResponse phaseResponse = buildPhaseResponseForCourseOrTest(feedback);
//                //updating the count
//                traineeRating.incrRating(feedback.getOverallRating());
//                traineeRating.incrCount();
//               courseResponseList.get(traineeRating.getIndex()).reviewer().getPhases().add(phaseResponse);
//                courseResponseList.get(traineeRating.getIndex()).getRecords().add(phaseResponse);
//                // courseResponseList.get(idx).reviewer().getPhases().add(phaseResponse);
//            }
//        }
//        //set overall rating in list
//        for (TraineeRating rating : dp.values()){
//            int index = rating.getIndex();
//               courseResponseList.get(index).reviewer().setOverallRating(roundOff_Rating(rating.getRating()/rating.getCount()));
//            courseResponseList.get(index).setOverallRating(roundOff_Rating(rating.getRating()/rating.getCount()));
//        }
//        courseResponseList = addingPhaseAndTestNameInCourseResponse(courseResponseList);
//        return courseResponseList;
        return null;
    }
}
