package com.chicmic.trainingModule.Service.FeedBackService;

import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponse;
import com.chicmic.trainingModule.Dto.CourseResponse.CourseResponse;
import com.chicmic.trainingModule.Dto.DashboardDto.DashboardResponse;
import com.chicmic.trainingModule.Dto.DashboardDto.FeedbackResponseDto;
import com.chicmic.trainingModule.Dto.DashboardDto.RatingDto;
import com.chicmic.trainingModule.Dto.DashboardDto.RatingReponseDto;
import com.chicmic.trainingModule.Dto.*;
import com.chicmic.trainingModule.Dto.FeedbackResponseDto.FeedbackResponse_COURSE;
import com.chicmic.trainingModule.Dto.FeedbackResponseDto.FeedbackResponse_PPT;
import com.chicmic.trainingModule.Dto.FeedbackResponseDto.FeedbackResponse_TEST;
import com.chicmic.trainingModule.Dto.PhaseResponse.PhaseResponse;
import com.chicmic.trainingModule.Dto.ratings.Rating;
import com.chicmic.trainingModule.Dto.ratings.Rating_COURSE;
import com.chicmic.trainingModule.Dto.ratings.Rating_PPT;
import com.chicmic.trainingModule.Dto.ratings.Rating_TEST;
import com.chicmic.trainingModule.Entity.Feedback;
import com.chicmic.trainingModule.ExceptionHandling.ApiException;
import com.chicmic.trainingModule.Repository.FeedbackRepo;
import com.chicmic.trainingModule.TrainingModuleApplication;
import com.chicmic.trainingModule.Util.FeedbackUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.MergeOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Collation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

import static com.chicmic.trainingModule.TrainingModuleApplication.*;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

import java.util.stream.Collectors;

import static com.chicmic.trainingModule.Dto.FeedbackResponseDto.FeedbackResponse.getTypeOfFeedbackResponse;
import static com.chicmic.trainingModule.Util.FeedbackUtil.getFeedbackMessageBasedOnOverallRating;
import static com.chicmic.trainingModule.Util.FeedbackUtil.searchNameAndEmployeeCode;
import static com.chicmic.trainingModule.Util.RatingUtil.roundOff_Rating;
import static org.springframework.data.mongodb.core.aggregation.ArrayOperators.Filter.filter;

@Service
public class FeedbackService {
    private final FeedbackRepo feedbackRepo;
    private final MongoTemplate mongoTemplate;

    public FeedbackService(FeedbackRepo feedbackRepo, MongoTemplate mongoTemplate) {
        this.feedbackRepo = feedbackRepo;
        this.mongoTemplate = mongoTemplate;
    }
    //method for checking user completed his course  or not
    public boolean checkIfExists(String userId, Integer planType, String planId,String milestoneId, boolean isCompleted) {
        Query query = new Query(Criteria.where("userId").is(userId)
                .and("plans.phases.tasks").elemMatch(
                        Criteria.where("planType").is(planType)
                                .and("plan._id").is(new ObjectId(planId))
                                .and("milestones").elemMatch(
                                        Criteria.where("_id").is(new ObjectId(milestoneId))
                                                .and("isCompleted").is(isCompleted)
                                )
                )
        );

       // return mongoTemplate.findOne(query, YourDocumentClass.class, "assignTask");

        boolean flag =  mongoTemplate.exists(query, "assignTask");
//        Query query = new Query(Criteria.where("userId").is(userId)
//                .and("plans.phases.tasks").elemMatch(
//                        Criteria.where("planType").is(planType)
//                                .and("milestones._id").is(planId)
//                                .and("milestones.isCompleted").is(isCompleted)
//                )
//        );
//        boolean flag =  mongoTemplate.exists(query, "assignTask");
        if(!flag)
            throw new ApiException(HttpStatus.BAD_REQUEST,"Trainee is still working on it!!!");
        return true;
    }

    public FeedbackResponse1 addingPhaseAndTestNameInResponse(FeedbackResponse1 feedbackResponse1){
        List<String> courseId = new ArrayList<>();
        List<String> testId = new ArrayList<>();
        int type = feedbackResponse1.getFeedbackType();

        if(type == 1||type == 3)
            courseId.add(feedbackResponse1.getCourse().get_id());
        else if (type == 2)
            testId.add(feedbackResponse1.getTest().get_id());
        //fetch course and test details
        Map<String,Document> courseDetail = getCourseNameAndPhaseName(courseId);
        Map<String,Document> testDetail =  getTestNameAndMilestoneName(testId);
        if(feedbackResponse1.getFeedbackType() == 1 || feedbackResponse1.getFeedbackType() == 3){
            Document courseData = courseDetail.get(feedbackResponse1.getCourse().get_id());
            feedbackResponse1.getCourse().setCourseName((String) courseData.get("name"));
            if(feedbackResponse1.getFeedbackType() == 1){
                List<Document> documentList = (List<Document>) courseData.get("phases");
                int count = 0;
                for (Document document : documentList){
                    ++count;
                    if(document.get("_id") == null) continue;
                    if (document.get("_id").toString().equals(feedbackResponse1.getPhase().get_id()))
                        feedbackResponse1.getPhase().setName(String.format("Phase-%d",count));
                }
            }
        }else if (feedbackResponse1.getFeedbackType() == 2){
            Document testData = testDetail.get(feedbackResponse1.getTest().get_id());
            feedbackResponse1.getTest().setTestName((String) testData.get("testName"));
            List<Document> documentList = (List<Document>) testData.get("milestones");
            int count = 0;
            for (Document document : documentList){
                ++count;
                if(document.get("_id") == null) continue;
                if (document.get("_id").toString().equals(feedbackResponse1.getMilestone().get_id()))
                    feedbackResponse1.getMilestone().setName(String.format("Milestone-%d",count));
            }
        }
        return feedbackResponse1;
    }
//    public List<CourseResponse> addingPhaseAndTestNameInCourseResponse(List<CourseResponse> courseResponseList){
//        //fetch courseId and TestId
//        List<String> courseId = new ArrayList<>();
//        List<String> testId = new ArrayList<>();
//        for (var courseResponse : courseResponseList) {
//            for ()
//        }
////        for (var courseResponse : courseResponseList){
////            int type = getTypeOfFeedbackResponse(feedbackResponse);
////            if(type == 1){
////                FeedbackResponse_COURSE feedbackResponseCourse = (FeedbackResponse_COURSE) feedbackResponse;
////                courseId.add(feedbackResponseCourse.getTask().get_id());
////            } else if (type == 2) {
////                FeedbackResponse_TEST feedbackResponseTest = (FeedbackResponse_TEST) feedbackResponse;
////                testId.add(feedbackResponseTest.getTask().get_id());
////            } else if (type == 3) {
////                FeedbackResponse_PPT feedbackResponsePpt = (FeedbackResponse_PPT) feedbackResponse;
////                courseId.add(feedbackResponsePpt.getTask().get_id());
////            }
////        }
//        return courseResponseList;
//    }

    public List<com.chicmic.trainingModule.Dto.FeedbackResponseDto.FeedbackResponse>
            addingPhaseAndTestNameInResponse(List<com.chicmic.trainingModule.Dto.FeedbackResponseDto.FeedbackResponse> feedbackResponses){
        //fetch courseId and TestId
        List<String> courseId = new ArrayList<>();
        List<String> testId = new ArrayList<>();
        for (var feedbackResponse : feedbackResponses){
            int type = getTypeOfFeedbackResponse(feedbackResponse);
            if(type == 1){
                FeedbackResponse_COURSE feedbackResponseCourse = (FeedbackResponse_COURSE) feedbackResponse;
                courseId.add(feedbackResponseCourse.getTask().get_id());
            } else if (type == 2) {
                FeedbackResponse_TEST feedbackResponseTest = (FeedbackResponse_TEST) feedbackResponse;
                testId.add(feedbackResponseTest.getTask().get_id());
            } else if (type == 3) {
                FeedbackResponse_PPT feedbackResponsePpt = (FeedbackResponse_PPT) feedbackResponse;
                courseId.add(feedbackResponsePpt.getTask().get_id());
            }
        }
        //fetch course details
        Map<String,Document> courseDetail = getCourseNameAndPhaseName(courseId);
        Map<String,Document> testDetail =  getTestNameAndMilestoneName(testId);
        for (var feedbackResponse : feedbackResponses){
            int type = getTypeOfFeedbackResponse(feedbackResponse);
            if(type == 1){
                FeedbackResponse_COURSE feedbackResponseCourse = (FeedbackResponse_COURSE) feedbackResponse;
                Document courseData = courseDetail.get(feedbackResponseCourse.getTask().get_id());
                feedbackResponseCourse.getTask().setName((String) courseData.get("name"));
                List<Document> documentList = (List<Document>) courseData.get("phases");
                int count = 0;
                for (Document document : documentList){
                    ++count;
                    if(document.get("_id") == null) continue;
                    if (document.get("_id").toString().equals(feedbackResponseCourse.getSubTask().get_id()))
                        feedbackResponseCourse.getSubTask().setName(String.format("Phase-%d",count));
                }
            } else if (type == 2) {
                FeedbackResponse_TEST feedbackResponseTest = (FeedbackResponse_TEST) feedbackResponse;
                Document testData = testDetail.get(feedbackResponseTest.getTask().get_id());
                feedbackResponseTest.getTask().setName((String) testData.get("testName"));
                List<Document> documentList = (List<Document>) testData.get("milestones");
                int count = 0;
                for (Document document : documentList){
                    ++count;
                    if (document.get("_id").toString().equals(feedbackResponseTest.getSubTask().get_id()))
                        feedbackResponseTest.getSubTask().setName(String.format("Milestone-%d",count));
                }
            } else if (type == 3) {
                FeedbackResponse_PPT feedbackResponsePpt = (FeedbackResponse_PPT) feedbackResponse;
                Document pptData = courseDetail.get(feedbackResponsePpt.getTask().get_id());
                feedbackResponsePpt.getTask().setName((String) pptData.get("name"));
            }
        }
        return feedbackResponses;
    }
    public DashboardResponse findFeedbacksSummaryOfTrainee(String traineeId){
        Criteria criteria = Criteria.where("traineeID").is(traineeId);
        Query query = new Query(criteria);
        List<Feedback> feedbackList = mongoTemplate.find(query,Feedback.class);
        float totalRating = 0;
        int count = 0;
        DashboardResponse dashboardResponse = DashboardResponse.builder().feedbacks(new ArrayList<>()).build();
       // RatingReponseDto ratingReponseDto = RatingReponseDto.builder().build();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        List<RatingDto> ratingDtoList = new ArrayList<>();
        for (int i=0;i<4;i++) ratingDtoList.add(new RatingDto(0f,1));
        for (Feedback feedback : feedbackList){
            if(++count<5){
                dashboardResponse.getFeedbacks().add(FeedbackResponseDto.builder().date(formatter.format(new Date()))
                                .rating(feedback.getOverallRating())
                                .feedback(feedback.getComment())
                                .name(TrainingModuleApplication.searchNameById(feedback.getCreatedBy()))
                        .build());
            }
            int type = feedback.getType().charAt(0) - '1';
            ratingDtoList.get(type).incrTotalRating(feedback.getOverallRating());
            ratingDtoList.get(type).incrcount();
            totalRating += feedback.getOverallRating();
        }
        float overallRating = compute_rating(totalRating,feedbackList.size());
        RatingReponseDto ratingReponseDto = RatingReponseDto.builder().overall(overallRating)
                .course(compute_rating(ratingDtoList.get(0).getTotalRating(),ratingDtoList.get(0).getCount()))
                .test(compute_rating(ratingDtoList.get(1).getTotalRating(),ratingDtoList.get(1).getCount()))
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
    public Float getOverallRatingOfTrainee(String traineeId){
        Criteria criteria = Criteria.where("traineeID").is(traineeId).and("isDeleted").is(false);
        Query query = new Query(criteria);
        List<Feedback> feedbackList = mongoTemplate.find(query,Feedback.class);
        float totalRating = 0;
        for (Feedback feedback : feedbackList){
            totalRating += feedback.getOverallRating();
        }
        return roundOff_Rating(totalRating/feedbackList.size());
    }

    public ApiResponse findTraineeFeedbacks(Integer pageNumber, Integer pageSize, String query, Integer sortDirection, String sortKey,String traineeId){
        Pageable pageable;
        List<Document> userDatasDocuments = idUserMap.values().stream().map(userDto ->
                        new Document("reviewerName",userDto.getName()).append("reviewerTeam",userDto.getTeamName()).append("reviewerCode",userDto.getEmpCode())
                                .append("id",userDto.get_id()))
                .toList();

        System.out.println(userDatasDocuments.size() + "///");
        Criteria criteria = Criteria.where("traineeID").is(traineeId)
                .and("isDeleted").is(false);

        //searching!!!
        if(query==null || query.isBlank()) query = ".*";
        int skipValue = (pageNumber - 1) * pageSize;

        System.out.println(userDatasDocuments.size() + "///");
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
        List<Feedback> feedbackList = mongoTemplate.aggregate(aggregation, "feedback", Feedback.class).getMappedResults();

        List<com.chicmic.trainingModule.Dto.FeedbackResponseDto.FeedbackResponse> feedbackResponses = new ArrayList<>();
        for (Feedback feedback : feedbackList) {
            feedbackResponses.add(com.chicmic.trainingModule.Dto.FeedbackResponseDto.FeedbackResponse.buildFeedbackResponse(feedback));
        }
        feedbackResponses = addingPhaseAndTestNameInResponse(feedbackResponses);
        long count = mongoTemplate.count(new Query(criteria),Feedback.class);
        return new ApiResponse(200, "List of All feedbacks", feedbackResponses,count);
    }
    //method for finding feedbacks given to a trainee
//    public ApiResponse findTraineeFeedbacks(Integer pageNumber, Integer pageSize, String query, Integer sortDirection, String sortKey,String traineeId){
//        Pageable pageable;
//        if (!sortKey.isEmpty()) {
//            Sort.Direction direction = (sortDirection == 1) ? Sort.Direction.ASC : Sort.Direction.DESC;
//            Sort sort = Sort.by(direction, sortKey);
//            pageable = PageRequest.of(pageNumber, pageSize, sort);
//        } else {
//            pageable = PageRequest.of(pageNumber, pageSize);
//        }
//        Criteria criteria = Criteria.where("traineeID").is(traineeId)
//                .and("isDeleted").is(false);
//
//        //temporary search query!!!
//        if(query!=null && !query.isBlank()) {
//            criteria.and("createdBy").in(searchNameAndEmployeeCode(query));
//        }
//        //get the count of trainee as well
//        long count = mongoTemplate.count(new Query(criteria),Feedback.class);
//        Query query1 = new Query(criteria).with(pageable);
//        List<Feedback> feedbackList =  mongoTemplate.find(query1,Feedback.class);
//
//        List<com.chicmic.trainingModule.Dto.FeedbackResponseDto.FeedbackResponse> feedbackResponses = new ArrayList<>();
//        for (Feedback feedback : feedbackList) {
//            feedbackResponses.add(com.chicmic.trainingModule.Dto.FeedbackResponseDto.FeedbackResponse.buildFeedbackResponse(feedback));
//        }
//        feedbackResponses = addingPhaseAndTestNameInResponse(feedbackResponses);
//        return new ApiResponse(200, "List of All feedbacks", feedbackResponses,count);
//    }

    public Feedback saveFeedbackInDB(FeedBackDto feedBackDto, String userId){
        //checking trainee exist in db!!!
        searchUserById(feedBackDto.getTrainee());
        //checking trainee Completed course or not!!!
        int type = feedBackDto.getFeedbackType().charAt(0) - '0';
        if(type == 1)
            checkIfExists(feedBackDto.getTrainee(),1,feedBackDto.getCourse(),feedBackDto.getPhase(),true);
        else if(type == 2)
            checkIfExists(feedBackDto.getTrainee(),2,feedBackDto.getTest(),feedBackDto.getMilestone(),true);

        //checking feedback exist in db!!!
        Feedback feedback1 = feedbackExist(feedBackDto,userId);
        if(feedback1 != null) throw new ApiException(HttpStatus.BAD_REQUEST,"Feedback submitted previously!");

        Rating rating = Rating.getRating(feedBackDto);
        Float overallRating = Rating.computeOverallRating(feedBackDto);
        Date currentDate = new Date(System.currentTimeMillis());

        Feedback feedback = Feedback.builder()
                .traineeID(feedBackDto.getTrainee())
                .rating(rating)
                .type(feedBackDto.getFeedbackType())
                .comment(feedBackDto.getComment())
                .createdAt(currentDate)
                .updateAt(currentDate)
                .createdBy(userId)
                .overallRating(roundOff_Rating(overallRating))
                .build();

        return feedbackRepo.save(feedback);
    }

    public String deleteFeedbackById(String id,String userId){
        Criteria criteria = Criteria.where("id").is(id).and("createdBy").is(userId);
        Query query = new Query(criteria);
        Update update = new Update().set("isDeleted",true);
//        DeleteResult deleteResult = mongoTemplate.remove(query,Feedback.class);
//        UpdateResult updateResult = mongoTemplate.updateFirst(query,update,Feedback.class);
            Feedback feedback = mongoTemplate.findAndModify(query,update,Feedback.class);
            if(feedback == null)  throw new ApiException(HttpStatus.valueOf(400),"Invalid traineeId");
            return feedback.getTraineeID();
//        if(updateResult.getModifiedCount() == 0) throw new ApiException(HttpStatus.valueOf(400),"Something went wrong!!");
    }

    public Feedback updateFeedback(FeedBackDto feedBackDto,String userId){
        //checking trainee exist in db!!!
        searchUserById(feedBackDto.getTrainee());
        //checking trainee Completed course or not!!!
        int type = feedBackDto.getFeedbackType().charAt(0) - '0';
        if(type == 1)
            checkIfExists(feedBackDto.getTrainee(),1,feedBackDto.getCourse(),feedBackDto.getPhase(),true);
        else if(type == 2)
            checkIfExists(feedBackDto.getTrainee(),2,feedBackDto.getTest(),feedBackDto.getMilestone(),true);

        //checking feedback exist in db!!!
       Feedback feedback1 = feedbackExist(feedBackDto,userId);
//        Optional<Feedback> feedbackOptional = feedbackRepo.findById(feedBackDto.get_id());
        Criteria criteria1 = Criteria.where("id").is(feedBackDto.get_id()).and("createdBy").is(userId);
        boolean flag = mongoTemplate.exists(new Query(criteria1),Feedback.class);
        if(!flag)
            throw new ApiException(HttpStatus.BAD_REQUEST,"You can't update this feedback!");

        if(feedback1 != null && !feedback1.getId().equals(feedBackDto.get_id()))
            throw new ApiException(HttpStatus.BAD_REQUEST,"Feedback submitted previously!");

        String _id = feedBackDto.get_id();
        Rating rating = Rating.getRating(feedBackDto);
        Float overallRating = Rating.computeOverallRating(feedBackDto);

        Criteria criteria = Criteria.where("id").is(_id).and("createdBy").is(userId);

        Query query = new Query(criteria);
        Update update = new Update()
                .set("updateAt",new Date(System.currentTimeMillis()))
                .set("comment",feedBackDto.getComment())
                .set("traineeID",feedBackDto.getTrainee())
                .set("type",feedBackDto.getFeedbackType())
                .set("overallRating",roundOff_Rating(overallRating))
                .set("rating",rating);

        FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true);

        return mongoTemplate.findAndModify(query,update,options,Feedback.class);
    }


    public FeedbackResponse buildFeedbackResponse(Feedback feedback){
        String taskName = null,subTask = null;
        int feedbackTypeId = feedback.getType().charAt(0) - '1';
        if(feedbackTypeId == 0){
            Rating_COURSE ratingCourse = (Rating_COURSE) feedback.getRating();
            taskName = ratingCourse.getCourseId();
            subTask = ratingCourse.getPhaseId();
        }else if(feedbackTypeId == 1){
            Rating_TEST ratingTest = (Rating_TEST) feedback.getRating();
            taskName = ratingTest.getTestId();
            subTask = ratingTest.getMilestoneId();
        }
//        System.out.println(FeedbackUtil.FEEDBACK_TYPE_CATEGORY[feedbackTypeId] + "PPPPPPPPPP");
        //UserDto userDto = UserDto.builder().name("Naman").teamId("Angular").empCode("CHM/2023/567").build();//searchUserById(feedback.getTraineeID());
        UserDto userDto = searchUserById(feedback.getTraineeID());
        return FeedbackResponse.builder().
                _id(feedback.getId())
               // .reviewer("Ankit Sir")
                .reviewer(TrainingModuleApplication.searchNameById(feedback.getCreatedBy()))
                .createdOn(feedback.getCreatedAt())
                .team(userDto.getTeamName())
                .type(FeedbackUtil.FEEDBACK_TYPE_CATEGORY[feedbackTypeId])
                .feedbackType(feedbackTypeId + 1)
                .employeeFullName(userDto.getName())
                .employeeCode(userDto.getEmpCode())
                .taskName(taskName)
                .subTask(subTask)
                .rating(feedback.getOverallRating())
                .comment(feedback.getComment())
            .build();
    }
//    public Float getOverallRatingOfTrainee(String traineeId){
//        Criteria criteria = Criteria.where("traineeID").is(traineeId);
//    }
    public HashMap<String,Object> getOverallRatingOfTrainee(String traineeId,String courseId,String phaseId){
//        MatchOperation matchOperation = new MatchOperation(Criteria.where("traineeID").is(traineeId)
//                .and("type").is("1").and("courseId").is(courseId)
//        );
//        Aggregation aggregation = newAggregation(matchOperation,);
//        mongoTemplate.aggregate(aggregation, "feedback", Document.class).getMappedResults();
        Criteria criteria = Criteria.where("traineeID").is(traineeId);
//                .and("type").is("1").and("courseId").is(courseId);
        Query query = new Query(criteria);
        List<Feedback> feedbackList = mongoTemplate.find(query,Feedback.class);
        float total = 0;
        float tr = 0,cnt = 0;
        float pr = 0,pcnt = 0;
        for (Feedback feedback : feedbackList){
            total += feedback.getOverallRating();
            if(feedback.getType().equals("1")){
                Rating_COURSE ratingCourse = (Rating_COURSE) feedback.getRating();
                if(!ratingCourse.getCourseId().equals(courseId)) continue;
                tr += feedback.getOverallRating();
                cnt += 1;
                if(ratingCourse.getPhaseId().equals(phaseId)){
                    pr += feedback.getOverallRating();
                    pcnt += 1;
                }
            }
        }
        total /= feedbackList.size();
        tr /= cnt;
        pr /= pcnt;
        HashMap<String,Object> rating = new HashMap<>();
        rating.put("overallRating",roundOff_Rating(total));
        rating.put("courseRating",roundOff_Rating(tr));
        rating.put("phaseRating",roundOff_Rating(pr));
        return rating;
    }

    public long countDocuments(Criteria criteria){
        Query query = new Query(criteria);
        return mongoTemplate.count(query,Feedback.class);
    }
    public List<CourseResponse> buildFeedbackResponseForCourseAndTest(List<Feedback> feedbackList,String _id,Integer type){
        Map<String,String> names = new HashMap<>();
        if(type == 1) names = getPhaseName(_id);
        else if (type == 2) names = getTestName(_id);
        else if(type == 3) names = getCoursesName(feedbackList);


        HashMap<String,TraineeRating> dp = new HashMap<>();
//        List<Reviewer> courseResponseList = new ArrayList<>();
        List<CourseResponse> courseResponseList = new ArrayList<>();
        for(Feedback feedback : feedbackList){
            String reviewerId = feedback.getCreatedBy();
            if(dp.get(reviewerId) == null) {//not present
                UserDto reviewer = TrainingModuleApplication.searchUserById(reviewerId);

                //creating a new element for reviewer
                CourseResponse courseResponse = CourseResponse.builder()
                        ._id(reviewerId)
                        .reviewerName(reviewer.getName())
                        .code(reviewer.getEmpCode())
                        .overallRating(5.0f)
                        .records(new ArrayList<>())
                        .build();

                PhaseResponse phaseResponse = buildPhaseResponseForCourseOrTest(feedback,names);
                //adding the phase into it
                courseResponse.getRecords().add(phaseResponse);
                dp.put(reviewerId,new TraineeRating(courseResponseList.size(),feedback.getOverallRating(),1));
//                courseResponseList.add(new Reviewer(courseResponse));
                courseResponseList.add(courseResponse);
            }else{
                //int idx = dp.get(reviewerId).getIndex();
                TraineeRating traineeRating = dp.get(reviewerId);
                PhaseResponse phaseResponse = buildPhaseResponseForCourseOrTest(feedback);
                //updating the count
                traineeRating.incrRating(feedback.getOverallRating());
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
//        courseResponseList = addingPhaseAndTestNameInCourseResponse(courseResponseList);
        return courseResponseList;
    }
    public List<CourseResponse> buildFeedbackResponseForCourseAndTest(List<Feedback> feedbackList){
        HashMap<String,TraineeRating> dp = new HashMap<>();
//        List<Reviewer> courseResponseList = new ArrayList<>();
        List<CourseResponse> courseResponseList = new ArrayList<>();
        for(Feedback feedback : feedbackList){
            String reviewerId = feedback.getCreatedBy();
            if(dp.get(reviewerId) == null) {//not present
                UserDto reviewer = TrainingModuleApplication.searchUserById(reviewerId);

                //creating a new element for reviewer
                CourseResponse courseResponse = CourseResponse.builder()
                        ._id(reviewerId)
                        .reviewerName(reviewer.getName())
                        .code(reviewer.getEmpCode())
                        .overallRating(5.0f)
                        .records(new ArrayList<>())
                        .build();

                PhaseResponse phaseResponse = buildPhaseResponseForCourseOrTest(feedback);
                //adding the phase into it
                courseResponse.getRecords().add(phaseResponse);
                dp.put(reviewerId,new TraineeRating(courseResponseList.size(),feedback.getOverallRating(),1));
//                courseResponseList.add(new Reviewer(courseResponse));
                courseResponseList.add(courseResponse);
            }else{
                //int idx = dp.get(reviewerId).getIndex();
                TraineeRating traineeRating = dp.get(reviewerId);
                PhaseResponse phaseResponse = buildPhaseResponseForCourseOrTest(feedback);
                //updating the count
                traineeRating.incrRating(feedback.getOverallRating());
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
//        courseResponseList = addingPhaseAndTestNameInCourseResponse(courseResponseList);
        return courseResponseList;
    }

    public List<CourseResponse> findFeedbacksByCourseIdAndPhaseIdAndTraineeId(String courseId,String phaseId,String traineeId){
        Criteria criteria = Criteria.where("traineeID").is(traineeId).and("type").is("1")
                .and("isDeleted").is(false)
                .and("rating.courseId").is(courseId).and("rating.phaseId").is(phaseId);
//        criteria.elemMatch(new Criteria().and("rating.courseId").is(courseId).and("rating.phaseId").is(phaseId));
        Query query = new Query(criteria);
        List<Feedback> feedbackList = mongoTemplate.find(query, Feedback.class);

//        List<CourseResponse> courseResponseList = buildFeedbackResponseForCourseAndTest(feedbackList);
        List<CourseResponse> courseResponseList = buildFeedbackResponseForCourseAndTest(feedbackList,courseId,1);
        return courseResponseList;
    }

    public List<CourseResponse> findFeedbacksByTestIdAndPMilestoneIdAndTraineeId(String testId,String milestoneid,String traineeId){
        Criteria criteria = Criteria.where("traineeID").is(traineeId).and("type").is("2")
                .and("isDeleted").is(false)
                .and("rating.testId").is(testId).and("rating.milestoneId").is(milestoneid);
        Query query = new Query(criteria);
        List<Feedback> feedbackList = mongoTemplate.find(query,Feedback.class);
//        List<CourseResponse> testResponseList = buildFeedbackResponseForCourseAndTest(feedbackList);
        List<CourseResponse> testResponseList = buildFeedbackResponseForCourseAndTest(feedbackList,testId,2);
        return testResponseList;
    }
    public List<CourseResponse> findFeedbacksForCourseByCourseIdAndTraineeId(String courseId,String traineeId){
        Criteria criteria = Criteria.where("traineeID").is(traineeId).and("type").is("3")
                .and("isDeleted").is(false)
                .and("rating.courseId").is(courseId);
        Query query = new Query(criteria);
        List<Feedback> feedbackList = mongoTemplate.find(query,Feedback.class);
//        List<CourseResponse> testResponseList = buildFeedbackResponseForCourseAndTest(feedbackList);
        List<CourseResponse> testResponseList = buildFeedbackResponseForCourseAndTest(feedbackList,"vsdv",3);
        return testResponseList;
    }
    public PhaseResponse buildPhaseResponseForCourseOrTest(Feedback  feedback,Map<String,String> name){
        PhaseResponse phaseResponse = PhaseResponse.builder()
                .comment(feedback.getComment())
                .overallRating(feedback.getOverallRating())
                .createdAt(feedback.getCreatedAt())
                .build();

        if(feedback.getType().equals("1")) {
            Rating_COURSE ratingCourse = (Rating_COURSE) feedback.getRating();
            phaseResponse.set_id(ratingCourse.getPhaseId());
            phaseResponse.setName(name.get(ratingCourse.getPhaseId()));
            phaseResponse.setTheoreticalRating(ratingCourse.getTheoreticalRating());
            phaseResponse.setCommunicationRating(ratingCourse.getCommunicationRating());
            phaseResponse.setTechnicalRating(ratingCourse.getTechnicalRating());
        }else if(feedback.getType().equals("2")){
            Rating_TEST ratingTest = (Rating_TEST) feedback.getRating();
            phaseResponse.set_id(ratingTest.getMilestoneId());
            phaseResponse.setName(name.get(ratingTest.getMilestoneId()));
            phaseResponse.setTheoreticalRating(ratingTest.getTheoreticalRating());
            phaseResponse.setCommunicationRating(ratingTest.getCommunicationRating());
            phaseResponse.setCodingRating(ratingTest.getCodingRating());
        }else{
            Rating_PPT rating_ppt = (Rating_PPT) feedback.getRating();
            phaseResponse.set_id(rating_ppt.getCourseId());
            phaseResponse.setName(name.get(rating_ppt.getCourseId()));
            phaseResponse.setPresentationRating(rating_ppt.getPresentationRating());
            phaseResponse.setCommunicationRating(rating_ppt.getCommunicationRating());
            phaseResponse.setTechnicalRating(rating_ppt.getTechnicalRating());
        }
        return phaseResponse;
    }
    public PhaseResponse buildPhaseResponseForCourseOrTest(Feedback  feedback){
        PhaseResponse phaseResponse = PhaseResponse.builder()
                .comment(feedback.getComment())
                .overallRating(feedback.getOverallRating())
                .createdAt(feedback.getCreatedAt())
                .build();

        if(feedback.getType().equals("1")) {
            Rating_COURSE ratingCourse = (Rating_COURSE) feedback.getRating();
            phaseResponse.set_id(ratingCourse.getPhaseId());
            phaseResponse.setName(ratingCourse.getPhaseId());
            phaseResponse.setTheoreticalRating(ratingCourse.getTheoreticalRating());
            phaseResponse.setCommunicationRating(ratingCourse.getCommunicationRating());
            phaseResponse.setTechnicalRating(ratingCourse.getTechnicalRating());
        }else if(feedback.getType().equals("2")){
            Rating_TEST ratingTest = (Rating_TEST) feedback.getRating();
            phaseResponse.set_id(ratingTest.getMilestoneId());
            phaseResponse.setName(ratingTest.getMilestoneId());
            phaseResponse.setTheoreticalRating(ratingTest.getTheoreticalRating());
            phaseResponse.setCommunicationRating(ratingTest.getCommunicationRating());
            phaseResponse.setCodingRating(ratingTest.getCodingRating());
        }else{
            Rating_PPT rating_ppt = (Rating_PPT) feedback.getRating();
            phaseResponse.set_id(rating_ppt.getCourseId());
            phaseResponse.setName(rating_ppt.getCourseId());
            phaseResponse.setPresentationRating(rating_ppt.getPresentationRating());
            phaseResponse.setCommunicationRating(rating_ppt.getCommunicationRating());
            phaseResponse.setTechnicalRating(rating_ppt.getTechnicalRating());
        }
        return phaseResponse;
    }

    public FeedbackResponse1 buildFeedbackResponseForSpecificFeedback(Feedback feedback){
        return FeedbackResponse1.buildResponse(feedback);
    }
    public List<Feedback> findFeedbacksByTestIdAndTraineeId(String testId,String traineeId,String feedbackType){
        searchUserById(traineeId);

        Criteria criteria = Criteria.where("traineeID").is(traineeId).and("type").is("2")
                .and("isDeleted").is(false)
                .and("rating.testId").is(testId);
        Query query = new Query(criteria);
        return mongoTemplate.find(query, Feedback.class);
    }

    public List<Feedback> findFeedbacksByPptIdAndTraineeId(String traineeId,String feedbackType){
        searchUserById(traineeId);
        Criteria criteria = Criteria.where("traineeID").is(traineeId)
                .and("isDeleted").is(false)
                .and("type").is("3");
        Query query = new Query(criteria);
        return mongoTemplate.find(query,Feedback.class);
    }

    public List<Feedback> findFeedbacksByCourseIdAndTraineeId(String courseId,String traineeId,String feedbackType){
        searchUserById(traineeId);
        //find courseName!!!
        Criteria criteria = Criteria.where("traineeID").is(traineeId)
                .and("isDeleted").is(false)
                .and("type").is(feedbackType)
                .and("rating.courseId").is(courseId);
        Query query = new Query(criteria);

        return mongoTemplate.find(query, Feedback.class);
    }
    public FeedbackResponseForCourse buildFeedbackResponseForCourse(Feedback feedback){
        UserDto userDto = searchUserById(feedback.getTraineeID());
        Rating_COURSE ratingCourse = (Rating_COURSE) feedback.getRating();

        return FeedbackResponseForCourse.builder()
                ._id(feedback.getId())
                .reviewer(TrainingModuleApplication.searchNameById(feedback.getCreatedBy()))
                .createdOn(feedback.getCreatedAt())
                .team(userDto.getTeamName())
                .type("COURSE")
                .employeeFullName(userDto.getName())
                .employeeCode(userDto.getEmpCode())
                .rating(feedback.getOverallRating())
                .course(ratingCourse.getCourseId())
                .phase(ratingCourse.getPhaseId())
                .communicationRating(ratingCourse.getCommunicationRating())
                .theoreticalRating(ratingCourse.getTheoreticalRating())
                .technicalRating(ratingCourse.getTechnicalRating())
                .comment(feedback.getComment())
                .build();
    }
    public Feedback getFeedbackById(String id){
        Criteria criteria = Criteria.where("id").is(id).and("isDeleted").is(false);
        Query query = new Query(criteria);
        Feedback feedback = mongoTemplate.findOne(query,Feedback.class);
        if (feedback == null)
            throw new ApiException(HttpStatus.valueOf(404),"Please enter valid feedback id.");
        return feedback;
        //return feedbackRepo.findById(id);
    }
    public ApiResponse findFeedbacksGivenByEmployee(Integer pageNumber, Integer pageSize, String query, Integer sortDirection, String sortKey,String reviewer){
        List<Document> userDatasDocuments = idUserMap.values().stream().map(userDto ->
                        new Document("name",userDto.getName()).append("teamName",userDto.getTeamName()).append("empCode",userDto.getEmpCode())
                                .append("id",userDto.get_id()))
                .toList();

        System.out.println(userDatasDocuments.size() + "///");
        Criteria criteria = Criteria.where("createdBy").is(reviewer)
                .and("isDeleted").is(false);

//        //searching!!!
        if(query==null || query.isBlank()) {
            query = ".*";
        }

        final String key;
        //sorting!!!
        if(sortKey.equals("name")||sortKey.equals("empCode")||sortKey.equals("teamName")){
            key = String.format("userData.%s",sortKey);
        }else
            key = sortKey;

        System.out.println(userDatasDocuments.size() + "///");
        java.util.regex.Pattern namePattern = java.util.regex.Pattern.compile(query, java.util.regex.Pattern.CASE_INSENSITIVE);
        Aggregation aggregation = newAggregation(
                match(criteria),
//                context -> new Document("$match", new Document()), // Your match criteria here
//                context -> new Document("$addFields", new Document("person", new Document("name", "runjan").append("age", 50))),
                context -> new Document("$addFields", new Document("userDatas", userDatasDocuments)),
                context -> new Document("$addFields", new Document("userData",
                        new Document("$filter",
                                new Document("input", "$userDatas")
                                        .append("as", "user")
                                        .append("cond", new Document("$eq", Arrays.asList("$$user.id", "$traineeID")))
                        )
                )),
                context -> new Document("$unwind",
                        new Document("path", "$userData")
                                .append("preserveNullAndEmptyArrays", true)
                ),
                context -> new Document("$project", new Document("userDatas", 0)),
                context -> new Document("$match", new Document("$or", Arrays.asList(
                        new Document("userData.name", new Document("$regex", namePattern)),
                        new Document("userData.team",new Document("$regex",namePattern))// Search by 'team' field, without case-insensitive regex
                ))),
                context -> new Document("$sort", new Document(key, -1))
        );

        // Execute the aggregation
        List<Document> results = mongoTemplate.aggregate(aggregation, "feedback", Document.class).getMappedResults();
//          return mongoTemplate.aggregate(aggregation, "feedback", Document.class).getMappedResults();
//        System.out.println(results.get(0).get("_id") + "/////");
       // return results;
        long count = mongoTemplate.count(new Query(criteria),Feedback.class);
        return new ApiResponse(200,"List of All feedbacks",results);
    }
//    public ApiResponse findFeedbacks(Integer pageNumber, Integer pageSize, String query, Integer sortDirection, String sortKey,String reviewer){
//        Pageable pageable;
//        if (!sortKey.isEmpty()) {
//            Sort.Direction direction = (sortDirection == 1) ? Sort.Direction.ASC : Sort.Direction.DESC;
//            Sort sort = Sort.by(direction, sortKey);
//            pageable = PageRequest.of(pageNumber, pageSize, sort);
//        } else {
//            pageable = PageRequest.of(pageNumber, pageSize);
//        }
//
//        Criteria criteria = Criteria.where("createdBy").is(reviewer)
//                .and("isDeleted").is(false);
//        //temporary search query!!!
//        if(query!=null && !query.isBlank()) {
//            criteria.and("traineeID").in(searchNameAndEmployeeCode(query));
//        }
//        Query query1 = new Query(criteria).with(pageable);
//        query1.collation(Collation.of("en").strength(2));
//        List<Feedback> feedbackList =  mongoTemplate.find(query1,Feedback.class);
//        List<com.chicmic.trainingModule.Dto.FeedbackResponseDto.FeedbackResponse> feedbackResponses = new ArrayList<>();
//        for (Feedback feedback : feedbackList) {
//            feedbackResponses.add(com.chicmic.trainingModule.Dto.FeedbackResponseDto.FeedbackResponse.buildFeedbackResponse(feedback));
//        }
//        feedbackResponses = addingPhaseAndTestNameInResponse(feedbackResponses);
//        long count = mongoTemplate.count(query1,Feedback.class);
//        return new ApiResponse(200, "List of All feedbacks", feedbackResponses,count);
//        //return feedbackRepo.findAll(pageable);
//    }
    public ApiResponse findFeedbacks(Integer pageNumber, Integer pageSize, String query, Integer sortDirection, String sortKey,String reviewer){
        Pageable pageable;
        List<Document> userDatasDocuments = idUserMap.values().stream().map(userDto ->
                        new Document("traineeName",userDto.getName()).append("traineeTeam",userDto.getTeamName()).append("traineeCode",userDto.getEmpCode())
                                .append("id",userDto.get_id()))
                .toList();

        System.out.println(userDatasDocuments.size() + "///");
        Criteria criteria = Criteria.where("createdBy").is(reviewer)
                .and("isDeleted").is(false);

       //searching!!!
        if(query==null || query.isBlank()) query = ".*";
        int skipValue = (pageNumber - 1) * pageSize;

        System.out.println(userDatasDocuments.size() + "///");
        java.util.regex.Pattern namePattern = java.util.regex.Pattern.compile(query, java.util.regex.Pattern.CASE_INSENSITIVE);
        Aggregation aggregation = newAggregation(
                match(criteria),
                context -> new Document("$addFields", new Document("userDatas", userDatasDocuments)),
                context -> new Document("$addFields", new Document("userData",
                        new Document("$filter",
                                new Document("input", "$userDatas")
                                        .append("as", "user")
                                        .append("cond", new Document("$eq", Arrays.asList("$$user.id", "$traineeID")))
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
        List<Feedback> feedbackList = mongoTemplate.aggregate(aggregation, "feedback", Feedback.class).getMappedResults();
//        List<Feedback> feedbackList =  mongoTemplate.find(query1,Feedback.class);
        List<com.chicmic.trainingModule.Dto.FeedbackResponseDto.FeedbackResponse> feedbackResponses = new ArrayList<>();
        for (Feedback feedback : feedbackList) {
            feedbackResponses.add(com.chicmic.trainingModule.Dto.FeedbackResponseDto.FeedbackResponse.buildFeedbackResponse(feedback));
        }
        feedbackResponses = addingPhaseAndTestNameInResponse(feedbackResponses);
        long count = mongoTemplate.count(new Query(criteria),Feedback.class);
        return new ApiResponse(200, "List of All feedbacks", feedbackResponses,count);
        //return feedbackRepo.findAll(pageable);
    }

    public List<Document> getAllFeedbacksOfEmployeeById(String traineeId){
        // Define your match criteria
//        AggregationOperation addFieldsOperation = context -> {
//            Document condExpr = new Document();
//            for (Map.Entry<String, UserDto> entry : TrainingModuleApplication.idUserMap.entrySet()) {
//                String traineeID = entry.getKey();
//                String teamName = entry.getValue().getTeamName();
//                condExpr.put(traineeID, new Document("$cond", List.of(new Document("$eq", List.of("$traineeID", traineeID)), teamName, "$$REMOVE")));
////                condExpr.put(traineeID, new Document("$cond", List.of(new Document("$eq", List.of("$traineeID", traineeID)), teamName, "$$REMOVE")));
////                condExpr.put(traineeID, new Document("$cond", List.of(new Document("$eq", List.of("$traineeID", traineeID)), teamName, "$$REMOVE")));
//            }
//            return new Document("$addFields", new Document("teamName", condExpr));
//        };
//
//        Aggregation aggregation = Aggregation.newAggregation(
//                addFieldsOperation,
//                Aggregation.match(Criteria.where("traineeID").exists(true)) // Match traineeID field
//                // Add other stages as needed
//        );
//        List<UserDto> userDetails = new ArrayList<>(idUserMap.values());
        List<Document> userDatasDocuments = findTraineeAndMap().values().stream().map(userDto ->
                            new Document("name",userDto.getName()).append("team",userDto.getTeamName()).append("empCode",userDto.getEmpCode())
                                    .append("_id",userDto.get_id()))
                    .toList();


        System.out.println(userDatasDocuments.size());
        Aggregation aggregation = newAggregation(
                context -> new Document("$addFields", new Document("userDatas",
                        userDatasDocuments
                )),
                context -> new Document("$unwind", new Document("path", "$userDatas").append("preserveNullAndEmptyArrays", true)),
                context -> new Document("$group", new Document("_id", "$userDatas._id")
                        .append("name", new Document("$first", "$userDatas.name"))
                        .append("team", new Document("$first", "$userDatas.team"))
                        .append("employeeCode", new Document("$first", "$userDatas.empCode"))
                        .append("plan", new Document("$addToSet",
                                new Document("$cond", Arrays.asList(
                                        new Document("$eq", Arrays.asList("$userId", "$userDatas._id")),
                                        new Document("name", new Document("$arrayElemAt", Arrays.asList("$plans.planName", 0)))
                                                .append("_id", new Document("$arrayElemAt", Arrays.asList("$plans._id", 0))),
                                        "$$REMOVE"
                                ))
                        ))
                )
        );
            return mongoTemplate.aggregate(aggregation, "assignTask", Document.class).getMappedResults();
//        System.out.println(results.get(0).get("_id") + "/////");
//        return null;
    }
    

    public Feedback feedbackExist(FeedBackDto feedBackDto,String reviewer){
        String feedback = feedBackDto.getFeedbackType();
        if(feedback.equals("1")){
            Criteria criteria = Criteria.where("type").is("1").and("createdBy")
                    .is(reviewer).and("traineeID").is(feedBackDto.getTrainee())
                    .and("rating.phaseId").is(feedBackDto.getPhase()).and("rating.courseId").is(feedBackDto.getCourse())
                    .and("isDeleted").is(false);
            Query query = new Query(criteria);
            return mongoTemplate.findOne(query,Feedback.class);
        }else if(feedback.equals("2")){
            Criteria criteria = Criteria.where("type").is("2").and("createdBy")
                    .is(reviewer).and("traineeID").is(feedBackDto.getTrainee())
                    .and("rating.milestoneId").is(feedBackDto.getMilestone()).and("rating.testId").is(feedBackDto.getTest())
                    .and("isDeleted").is(false);
            Query query = new Query(criteria);
            return mongoTemplate.findOne(query,Feedback.class);
        }else if(feedback.equals("3")){
            Criteria criteria = Criteria.where("type").is("3").and("createdBy")
                    .is(reviewer).and("traineeID").is(feedBackDto.getTrainee())
                    .and("rating.courseId").is(feedBackDto.getCourse())
                    .and("isDeleted").is(false);

            Query query = new Query(criteria);
            return mongoTemplate.findOne(query,Feedback.class);
        }
        // Get the current date
//        LocalDate currentDate = LocalDate.now();
//        // Get the current year
//        int currentYear = currentDate.getYear();
//        // Get the current month (as an integer)
//        int currentMonthValue = currentDate.getMonthValue();
//        String regexPattern = String.format("^%04d-%02d.*$", currentYear, currentMonthValue);
        Criteria criteria = Criteria.where("type").is("4").and("createdBy")
                .is(reviewer).and("traineeID").is(feedBackDto.getTrainee())
                .and("isDeleted").is(false);
//                .and("createdAt").regex(regexPattern);
        Query query = new Query(criteria);
        return mongoTemplate.findOne(query,Feedback.class);
    }

    public List<Feedback> getAllFeedbacksOfTraineeOnCourseWithId(String traineeId,String courseId){
        Criteria criteria = Criteria.where("traineeID").is(traineeId)
                .and("isDeleted").is(false)
                .and("type").is("1")
                .and("rating.courseId").is(courseId);
        Query query = new Query(criteria);

        return mongoTemplate.find(query,Feedback.class);
    }
    public List<Document> calculateEmployeeRatingSummary(Set<String> userIds) {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("traineeID").in(userIds).and("isDeleted").is(false)),
                Aggregation.group("traineeID")
                        .sum("overallRating").as("overallRating")
                        .count().as("count")
        );
        AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(aggregation, "feedback", Document.class);
        return aggregationResults.getMappedResults();
    }
    //courses method
    public Map<String,Document> getCourseNameAndPhaseName(List<String> _ids){
        Query query = Query.query(Criteria.where("_id").in(_ids));

       // Fields fields = Fields.fields("name", "phases._id", "phases.tasks._id");
        query.fields().include("_id","name","phases._id");

        List<Document> documentList =  mongoTemplate.find(query, Document.class, "course");

        Map<String,Document> coursesDetails = new HashMap<>();
        for (Document document : documentList)
            coursesDetails.put(document.get("_id").toString(),document);

        return coursesDetails;
    }
    public Map<String,String> getCoursesName(List<Feedback> feedbackList){
        List<String> _ids = new ArrayList<>();
        for (Feedback feedback : feedbackList){
            Rating_PPT rating_ppt = (Rating_PPT) feedback.getRating();
            _ids.add(rating_ppt.getCourseId());
        }
        Query query = Query.query(Criteria.where("_id").in(_ids));
        query.fields().include("_id","name");
        List<Document> documentList =  mongoTemplate.find(query, Document.class, "course");
        Map<String,String> coursesDetails = new HashMap<>();
        for (Document document : documentList)
            coursesDetails.put(document.get("_id").toString(),(String) document.get("name"));

        return coursesDetails;
    }
    public Map<String,String> getPhaseName(String _id){
        Query query = Query.query(Criteria.where("_id").is(_id));
        query.fields().include("name","phases._id");
        Document courseDetail = mongoTemplate.findOne(query,Document.class,"course");
        if(courseDetail == null) throw new ApiException(HttpStatus.BAD_REQUEST,"This courseId doesn't exist");
        List<Document> documentList = (List<Document>) courseDetail.get("phases");
        Map<String,String> phases =new HashMap<>();
        int count = 0;
        for (Document document : documentList){
            String phaseId = document.get("_id").toString();
            phases.put(phaseId,String.format("Phase - %s",++count));
        }
        return phases;
    }

    public Map<String, String> getTestName(String _id){
        Query query = Query.query(Criteria.where("_id").is(_id));
        query.fields().include("name","milestones._id");
        Document testDetail = mongoTemplate.findOne(query,Document.class,"test");
        if(testDetail == null) throw new ApiException(HttpStatus.BAD_REQUEST,"This testId doesn't exist");
        List<Document> documentList = (List<Document>) testDetail.get("milestones");
        Map<String,String> phases =new HashMap<>();
        int count = 0;
        for (Document document : documentList){
            String phaseId = document.get("_id").toString();
            phases.put(phaseId,String.format("Milestone - %s",++count));
        }
        return phases;
    }
    //
    public Map<String,Document> getTestNameAndMilestoneName(List<String> _ids){
        Query query = Query.query(Criteria.where("_id").in(_ids));

        // Fields fields = Fields.fields("name", "phases._id", "phases.tasks._id");
        query.fields().include("_id","testName","milestones._id");

        List<Document> documentList =  mongoTemplate.find(query, Document.class, "test");

        Map<String,Document> testDetails = new HashMap<>();
        for (Document document : documentList)
            testDetails.put(document.get("_id").toString(),document);

        return testDetails;
    }
    public String getFeedbackIdForMileStoneAndPhase(String type,String testId,String mileStoneId,String reviewerId,String traineeId){
        if(type.equals("1")){
            Criteria criteria = Criteria.where("traineeID").is(traineeId).and("type").is(type)
                    .and("createdBy").is(reviewerId)
                    .and("rating.courseId").is(testId)
                    .and("rating.phaseId").is(mileStoneId);
            Query query = new Query(criteria);
            query.fields().include("_id");
            Document document =  mongoTemplate.findOne(query, Document.class, "feedaback");
            if(document == null) return null;
            return document.get("_id").toString();
        }
        Criteria criteria = Criteria.where("traineeID").is(traineeId).and("type").is(type)
                .and("createdBy").is(reviewerId)
                .and("rating.testId").is(testId)
                .and("rating.milestoneId").is(mileStoneId);
        Query query = new Query(criteria);
        query.fields().include("_id");
        Document document =  mongoTemplate.findOne(query, Document.class, "feedaback");
        if(document == null) return  null;
        return document.get("_id").toString();
    }
    //create advance filters
}
