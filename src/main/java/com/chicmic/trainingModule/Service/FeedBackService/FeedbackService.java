package com.chicmic.trainingModule.Service.FeedBackService;

import com.chicmic.trainingModule.Dto.*;
import com.chicmic.trainingModule.Dto.CourseResponse.CourseResponse;
import com.chicmic.trainingModule.Dto.PhaseResponse.PhaseResponse;
import com.chicmic.trainingModule.Dto.PhaseResponse.PhaseResponse_COURSE;
import com.chicmic.trainingModule.Dto.PhaseResponse.PhaseResponse_TEST;
import com.chicmic.trainingModule.Dto.ratings.Rating;
import com.chicmic.trainingModule.Dto.ratings.Rating_COURSE;
import com.chicmic.trainingModule.Dto.ratings.Rating_TEST;
import com.chicmic.trainingModule.Entity.Feedback;
import com.chicmic.trainingModule.ExceptionHandling.ApiException;
import com.chicmic.trainingModule.Repository.FeedbackRepo;
import com.chicmic.trainingModule.TrainingModuleApplication;
import com.chicmic.trainingModule.Util.FeedbackUtil;
import com.mongodb.client.result.DeleteResult;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.chicmic.trainingModule.TrainingModuleApplication.searchUserById;
import static com.chicmic.trainingModule.Util.RatingUtil.roundOff_Rating;

@Service
public class FeedbackService {
    private final FeedbackRepo feedbackRepo;
    private final MongoTemplate mongoTemplate;

    public FeedbackService(FeedbackRepo feedbackRepo, MongoTemplate mongoTemplate) {
        this.feedbackRepo = feedbackRepo;
        this.mongoTemplate = mongoTemplate;
    }

    public Feedback saveFeedbackInDB(FeedBackDto feedBackDto, String userId){
        //checking trainee exist in db!!!
        searchUserById(feedBackDto.getTrainee());

        //checking feedback exist in db!!!
        boolean flag = feedbackExist(feedBackDto,userId);
        if(flag) throw new ApiException(HttpStatus.OK,"You already give feedback on this topic!!!");

        Rating rating = Rating.getRating(feedBackDto);
        Float overallRating = Rating.computeOverallRating(feedBackDto);
        Date currentDate = new Date(System.currentTimeMillis());

        Feedback feedback = Feedback.builder()
                .traineeID(feedBackDto.getTrainee())
                .rating(rating)
                .feedbackType(feedBackDto.getFeedbackType())
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
                .set("feedbackType",feedBackDto.getFeedbackType())
                .set("overallRating",roundOff_Rating(overallRating))
                .set("rating",rating);

        FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true);

        return mongoTemplate.findAndModify(query,update,options,Feedback.class);
    }


    public FeedbackResponse buildFeedbackResponse(Feedback feedback){
        String taskName = null,subTask = null;
        int feedbackTypeId = feedback.getFeedbackType().charAt(0) - '1';
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
    public List<Reviewer> buildFeedbackResponseForCourseAndTest(List<Feedback> feedbackList){
        HashMap<String,Integer> dp = new HashMap<>();
        List<Reviewer> courseResponseList = new ArrayList<>();
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
                        .phases(new ArrayList<>())
                        .build();

                PhaseResponse phaseResponse = buildPhaseResponseForCourseOrTest(feedback);
                //adding the phase into it
                courseResponse.getPhases().add(phaseResponse);
                dp.put(reviewerId,courseResponseList.size());
                courseResponseList.add(new Reviewer(courseResponse));
            }else{
                int idx = dp.get(reviewerId);
                PhaseResponse phaseResponse = buildPhaseResponseForCourseOrTest(feedback);
                courseResponseList.get(idx).reviewer().getPhases().add(phaseResponse);
            }
        }
        return courseResponseList;
    }
    public PhaseResponse buildPhaseResponseForCourseOrTest(Feedback  feedback){
        if(feedback.getFeedbackType().equals("1")) {
            Rating_COURSE ratingCourse = (Rating_COURSE) feedback.getRating();
            return PhaseResponse_COURSE.builder()
                    .phaseId(ratingCourse.getPhaseId())
                    .phaseName(ratingCourse.getPhaseId())
                    .createAt(feedback.getCreatedAt())
                    .overallRating(feedback.getOverallRating())
                    .technicalRating(ratingCourse.getTechnicalRating())
                    .communicationRating(ratingCourse.getCommunicationRating())
                    .theoreticalRating(ratingCourse.getTheoreticalRating())
                    .comment(feedback.getComment())
                    .build();
        }
        Rating_TEST ratingTest = (Rating_TEST) feedback.getRating();
        return PhaseResponse_TEST.builder()
                .milestoneId(ratingTest.getMilestoneId())
                .milestoneName(ratingTest.getMilestoneId())
                .overallRating(feedback.getOverallRating())
                .comment(feedback.getComment())
                .communicationRating(ratingTest.getCommunicationRating())
                .theoreticalRating(ratingTest.getTheoreticalRating())
                .codingRating(ratingTest.getCodingRating())
                .build();
    }

    public FeedbackResponse1 buildFeedbackResponseForSpecificFeedback(Feedback feedback){
        return FeedbackResponse1.buildResponse(feedback);
    }
    public List<Feedback> findFeedbacksByTestIdAndTraineeId(String testId,String traineeId,String feedbackType){
        Criteria criteria = Criteria.where("traineeID").is(traineeId).and("feedbackType").is("2")
                .and("rating.testId").is(testId);
        Query query = new Query(criteria);
        return mongoTemplate.find(query, Feedback.class);
    }
    public List<Feedback> findFeedbacksByCourseIdAndTraineeId(String courseId,String traineeId,String feedbackType){
        Criteria criteria = Criteria.where("traineeID").is(traineeId).and("feedbackType").is("1")
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
            Sort.Direction direction = (sortDirection == 0) ? Sort.Direction.ASC : Sort.Direction.DESC;
            Sort sort = Sort.by(direction, sortKey);
            pageable = PageRequest.of(pageNumber, pageSize, sort);
        } else {
            pageable = PageRequest.of(pageNumber, pageSize);
        }
        Criteria criteria = Criteria.where("createdBy").is(reviewer);
        return mongoTemplate.find(new Query(criteria).with(pageable),Feedback.class);
        //return feedbackRepo.findAll(pageable);
    }

    public List<Feedback> getAllFeedbacksOfEmployeeById(String traineeId){
        searchUserById(traineeId);
        return feedbackRepo.findAllByTraineeID(traineeId);
    }

    public boolean feedbackExist(FeedBackDto feedBackDto,String reviewer){
        String feedbcakType = feedBackDto.getFeedbackType();
        if(feedbcakType.equals("1")){
            Criteria criteria = Criteria.where("feedbackType").is("1").and("createdBy")
                    .is(reviewer).and("traineeID").is(feedBackDto.getTrainee())
                    .and("rating.phaseId").is(feedBackDto.getPhase()).and("rating.courseId").is(feedBackDto.getCourse());
            Query query = new Query(criteria);
            return mongoTemplate.exists(query,Feedback.class);
        }else if(feedbcakType.equals("2")){
            Criteria criteria = Criteria.where("feedbackType").is(feedBackDto.getFeedbackType()).and("createdBy")
                    .is(reviewer).and("traineeID").is(feedBackDto.getTrainee())
                    .and("rating.milestoneId").is(feedBackDto.getMilestone()).and("rating.testId").is(feedBackDto.getTest());;
            Query query = new Query(criteria);
            return mongoTemplate.exists(query,Feedback.class);
        }else if(feedbcakType.equals("3")){
            Criteria criteria = Criteria.where("feedbackType").is("3").and("createdBy")
                    .is(reviewer).and("traineeID").is(feedBackDto.getTrainee());

            Query query = new Query(criteria);
            return mongoTemplate.exists(query,Feedback.class);
        }
        Criteria criteria = Criteria.where("feedbackType").is("4").and("createdBy")
                .is(reviewer).and("traineeID").is(feedBackDto.getTrainee());
        Query query = new Query(criteria);
        return mongoTemplate.exists(query,Feedback.class);
    }
    public List<Feedback> getAllFeedbacksOfTraineeOnCourseWithId(String traineeId,String courseId){
        Criteria criteria = Criteria.where("traineeID").is(traineeId)
                .and("feedbackType").is("1")
                .and("rating.courseId").is(courseId);
        Query query = new Query(criteria);

        return mongoTemplate.find(query,Feedback.class);
    }

}
