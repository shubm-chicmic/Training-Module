package com.chicmic.trainingModule.Service.FeedBackService;

import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponse;
import com.chicmic.trainingModule.Dto.CourseResponse.CourseResponse;
import com.chicmic.trainingModule.Dto.*;
import com.chicmic.trainingModule.Dto.PhaseResponse.PhaseResponse;
import com.chicmic.trainingModule.Dto.ratings.Rating;
import com.chicmic.trainingModule.Dto.ratings.Rating_COURSE;
import com.chicmic.trainingModule.Dto.ratings.Rating_TEST;
import com.chicmic.trainingModule.Entity.Feedback;
import com.chicmic.trainingModule.ExceptionHandling.ApiException;
import com.chicmic.trainingModule.Repository.FeedbackRepo;
import com.chicmic.trainingModule.TrainingModuleApplication;
import com.chicmic.trainingModule.Util.FeedbackUtil;
import com.mongodb.client.result.DeleteResult;
import org.bson.Document;
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

import java.util.*;

import static com.chicmic.trainingModule.TrainingModuleApplication.searchUserById;
import static com.chicmic.trainingModule.Util.FeedbackUtil.searchNameAndEmployeeCode;
import static com.chicmic.trainingModule.Util.RatingUtil.roundOff_Rating;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Service
public class FeedbackService {
    private final FeedbackRepo feedbackRepo;
    private final MongoTemplate mongoTemplate;

    public FeedbackService(FeedbackRepo feedbackRepo, MongoTemplate mongoTemplate) {
        this.feedbackRepo = feedbackRepo;
        this.mongoTemplate = mongoTemplate;
    }
    public Float getOverallRatingOfTrainee(String traineeId){
        Criteria criteria = Criteria.where("traineeID").is(traineeId);
        Query query = new Query(criteria);
        List<Feedback> feedbackList = mongoTemplate.find(query,Feedback.class);
        float totalRating = 0;
        for (Feedback feedback : feedbackList){
            totalRating += feedback.getOverallRating();
        }
        return roundOff_Rating(totalRating/feedbackList.size());
    }
    //method for finding feedbacks given to a trainee
    public ApiResponse findTraineeFeedbacks(Integer pageNumber, Integer pageSize, String query, Integer sortDirection, String sortKey,String traineeId){
        Pageable pageable;
        if (!sortKey.isEmpty()) {
            Sort.Direction direction = (sortDirection == 1) ? Sort.Direction.ASC : Sort.Direction.DESC;
            Sort sort = Sort.by(direction, sortKey);
            pageable = PageRequest.of(pageNumber, pageSize, sort);
        } else {
            pageable = PageRequest.of(pageNumber, pageSize);
        }
        Criteria criteria = Criteria.where("traineeID").is(traineeId);

        //temporary search query!!!
        if(query!=null && !query.isBlank()) {
            criteria.and("createdBy").in(searchNameAndEmployeeCode(query));
        }
        //get the count of trainee as well
        long count = mongoTemplate.count(new Query(criteria),Feedback.class);
        Query query1 = new Query(criteria).with(pageable);
        List<Feedback> feedbackList =  mongoTemplate.find(query1,Feedback.class);

        List<com.chicmic.trainingModule.Dto.FeedbackResponseDto.FeedbackResponse> feedbackResponses = new ArrayList<>();
        for (Feedback feedback : feedbackList) {
            feedbackResponses.add(com.chicmic.trainingModule.Dto.FeedbackResponseDto.FeedbackResponse.buildFeedbackResponse(feedback));
        }
        return new ApiResponse(200, "List of All feedbacks", feedbackResponses,count);
    }

    public Feedback saveFeedbackInDB(FeedBackDto feedBackDto, String userId){
        //checking trainee exist in db!!!
        searchUserById(feedBackDto.getTrainee());

        //checking feedback exist in db!!!
        boolean flag = feedbackExist(feedBackDto,userId);
        if(flag) throw new ApiException(HttpStatus.BAD_REQUEST,"You already give feedback on this topic!!!");

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

    public void deleteFeedbackById(String id,String userId){
        Criteria criteria = Criteria.where("id").is(id).and("createdBy").is(userId);
        Query query = new Query(criteria);
        DeleteResult deleteResult = mongoTemplate.remove(query,Feedback.class);
        if(deleteResult.getDeletedCount() == 0) throw new ApiException(HttpStatus.valueOf(401),"Something went wrong!!");
    }

    public Feedback updateFeedback(FeedBackDto feedBackDto,String userId){
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
        return courseResponseList;
    }
    public List<CourseResponse> findFeedbacksByCourseIdAndPhaseIdAndTraineeId(String courseId,String phaseId,String traineeId){
        Criteria criteria = Criteria.where("traineeID").is(traineeId).and("type").is("1")
                .and("rating.courseId").is(courseId).and("rating.phaseId").is(phaseId);
//        criteria.elemMatch(new Criteria().and("rating.courseId").is(courseId).and("rating.phaseId").is(phaseId));
        Query query = new Query(criteria);
        List<Feedback> feedbackList = mongoTemplate.find(query, Feedback.class);

        List<CourseResponse> courseResponseList = buildFeedbackResponseForCourseAndTest(feedbackList);
        return courseResponseList;
    }

    public List<CourseResponse> findFeedbacksByTestIdAndPMilestoneIdAndTraineeId(String testId,String milestoneid,String traineeId){
        Criteria criteria = Criteria.where("traineeID").is(traineeId).and("type").is("2")
                .and("rating.testId").is(testId).and("rating.milestoneId").is(milestoneid);
        Query query = new Query(criteria);
        List<Feedback> feedbackList = mongoTemplate.find(query,Feedback.class);
        List<CourseResponse> testResponseList = buildFeedbackResponseForCourseAndTest(feedbackList);
        return testResponseList;
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
        }else{
            Rating_TEST ratingTest = (Rating_TEST) feedback.getRating();
            phaseResponse.set_id(ratingTest.getMilestoneId());
            phaseResponse.setName(ratingTest.getMilestoneId());
            phaseResponse.setTheoreticalRating(ratingTest.getTheoreticalRating());
            phaseResponse.setCommunicationRating(ratingTest.getCommunicationRating());
            phaseResponse.setCodingRating(ratingTest.getCodingRating());
        }
        return phaseResponse;
    }

    public FeedbackResponse1 buildFeedbackResponseForSpecificFeedback(Feedback feedback){
        return FeedbackResponse1.buildResponse(feedback);
    }
    public List<Feedback> findFeedbacksByTestIdAndTraineeId(String testId,String traineeId,String feedbackType){
        searchUserById(traineeId);

        Criteria criteria = Criteria.where("traineeID").is(traineeId).and("type").is("2")
                .and("rating.testId").is(testId);
        Query query = new Query(criteria);
        return mongoTemplate.find(query, Feedback.class);
    }
    public List<Feedback> findFeedbacksByCourseIdAndTraineeId(String courseId,String traineeId,String feedbackType){
        searchUserById(traineeId);

        Criteria criteria = Criteria.where("traineeID").is(traineeId).and("type").is("1")
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
    public Optional<Feedback> getFeedbackById(String id){
        return feedbackRepo.findById(id);
    }

    public List<Feedback> findFeedbacks(Integer pageNumber, Integer pageSize, String query, Integer sortDirection, String sortKey,String reviewer){
        Pageable pageable;
        if (!sortKey.isEmpty()) {
            Sort.Direction direction = (sortDirection == 1) ? Sort.Direction.ASC : Sort.Direction.DESC;
            Sort sort = Sort.by(direction, sortKey);
            pageable = PageRequest.of(pageNumber, pageSize, sort);
        } else {
            pageable = PageRequest.of(pageNumber, pageSize);
        }

        Criteria criteria = Criteria.where("createdBy").is(reviewer);
        //temporary search query!!!
        if(query!=null && !query.isBlank()) {
            criteria.and("traineeID").in(searchNameAndEmployeeCode(query));
        }
        Query query1 = new Query(criteria).with(pageable);
        query1.collation(Collation.of("en").strength(2));
        return mongoTemplate.find(query1,Feedback.class);
        //return feedbackRepo.findAll(pageable);
    }

    public List<Document> getAllFeedbacksOfEmployeeById(String traineeId){
        // Define your match criteria
        AggregationOperation addFieldsOperation = context -> {
            Document condExpr = new Document();
            for (Map.Entry<String, UserDto> entry : TrainingModuleApplication.idUserMap.entrySet()) {
                String traineeID = entry.getKey();
                String teamName = entry.getValue().getTeamName();
                condExpr.put(traineeID, new Document("$cond", List.of(new Document("$eq", List.of("$traineeID", traineeID)), teamName, "$$REMOVE")));
//                condExpr.put(traineeID, new Document("$cond", List.of(new Document("$eq", List.of("$traineeID", traineeID)), teamName, "$$REMOVE")));
//                condExpr.put(traineeID, new Document("$cond", List.of(new Document("$eq", List.of("$traineeID", traineeID)), teamName, "$$REMOVE")));
            }
            return new Document("$addFields", new Document("teamName", condExpr));
        };

        Aggregation aggregation = Aggregation.newAggregation(
                addFieldsOperation,
                Aggregation.match(Criteria.where("traineeID").exists(true)) // Match traineeID field
                // Add other stages as needed
        );
        return mongoTemplate.aggregate(aggregation, "feedback", Document.class).getMappedResults();
    }

    public boolean feedbackExist(FeedBackDto feedBackDto,String reviewer){
        String feedback = feedBackDto.getFeedbackType();
        if(feedback.equals("1")){
            Criteria criteria = Criteria.where("type").is("1").and("createdBy")
                    .is(reviewer).and("traineeID").is(feedBackDto.getTrainee())
                    .and("rating.phaseId").is(feedBackDto.getPhase()).and("rating.courseId").is(feedBackDto.getCourse());
            Query query = new Query(criteria);
            return mongoTemplate.exists(query,Feedback.class);
        }else if(feedback.equals("2")){
            Criteria criteria = Criteria.where("type").is("2").and("createdBy")
                    .is(reviewer).and("traineeID").is(feedBackDto.getTrainee())
                    .and("rating.milestoneId").is(feedBackDto.getMilestone()).and("rating.testId").is(feedBackDto.getTest());;
            Query query = new Query(criteria);
            return mongoTemplate.exists(query,Feedback.class);
        }else if(feedback.equals("3")){
            Criteria criteria = Criteria.where("type").is("3").and("createdBy")
                    .is(reviewer).and("traineeID").is(feedBackDto.getTrainee())
                    .and("rating.courseId").is(feedBackDto.getCourse());

            Query query = new Query(criteria);
            return mongoTemplate.exists(query,Feedback.class);
        }
        Criteria criteria = Criteria.where("type").is("4").and("createdBy")
                .is(reviewer).and("traineeID").is(feedBackDto.getTrainee());
        Query query = new Query(criteria);
        return mongoTemplate.exists(query,Feedback.class);
    }

    public List<Feedback> getAllFeedbacksOfTraineeOnCourseWithId(String traineeId,String courseId){
        Criteria criteria = Criteria.where("traineeID").is(traineeId)
                .and("type").is("1")
                .and("rating.courseId").is(courseId);
        Query query = new Query(criteria);

        return mongoTemplate.find(query,Feedback.class);
    }
    public List<Document> calculateEmployeeRatingSummary(List<String> userIds) {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("traineeID").in(userIds)),
                Aggregation.group("traineeID")
                        .sum("overallRating").as("overallRating")
                        .count().as("count")
        );
        AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(aggregation, "feedback", Document.class);
        return aggregationResults.getMappedResults();
    }
    //create advance filters
}
