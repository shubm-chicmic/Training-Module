package com.chicmic.trainingModule.Service.FeedBackService;

import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponse;
import com.chicmic.trainingModule.Dto.FeedbackDto.FeedbackRequestDto;
import com.chicmic.trainingModule.Dto.FeedbackResponseDto_V2.FeedbackResponse;
import com.chicmic.trainingModule.Dto.FeedbackResponse_V2;
import com.chicmic.trainingModule.Dto.rating.Rating;
import com.chicmic.trainingModule.Dto.rating.Rating_PPT;
import com.chicmic.trainingModule.Entity.Feedback_V2;
import com.chicmic.trainingModule.ExceptionHandling.ApiException;
import com.chicmic.trainingModule.TrainingModuleApplication;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.types.ObjectId;
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

    public FeedbackService_V2(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public boolean feedbackExist(Feedback_V2 feedback_v2,String reviewer){
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

    public Feedback_V2 saveFeedbackInDb(FeedbackRequestDto feedbackDto, String reviewerId){
        //checking traineeId is valid
        TrainingModuleApplication.searchUserById(feedbackDto.getTrainee());

        Feedback_V2 feedback = buildFeedbackFromFeedbackRequestDto(feedbackDto,reviewerId);
        //course assigned or not!!

        //checking feedback exist or not!!
        boolean flag = feedbackExist(feedback,reviewerId);
        if (flag) throw new ApiException(HttpStatus.BAD_REQUEST,"Feedback submitted previously!!");

        return mongoTemplate.insert(feedback,"feedback_V2");
    }

    public Feedback_V2 updateFeedback(FeedbackRequestDto feedbackRequestDto,String reviewerId){
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

        return mongoTemplate.findAndModify(new Query(criteria),update,options,Feedback_V2.class);
    }
    public String deleteFeedbackById(String _id,String reviewerId){
        Criteria criteria = Criteria.where("_id").is(_id).and("createdBy").is(reviewerId);
        Query query = new Query(criteria);
        Update update = new Update().set("isDeleted",true);
        Feedback_V2 feedback = mongoTemplate.findAndModify(query,update,Feedback_V2.class);
        if(feedback == null)  throw new ApiException(HttpStatus.valueOf(400),"You can't delete this feedback");

        return feedback.getTraineeId();
    }
    public Feedback_V2 getFeedbackById(String _id){
        System.out.println(_id + "////");
//        Criteria criteria = Criteria.where("_id").is(_id);
//        mongoTemplate
//        Feedback_V2 feedbackV2 =  mongoTemplate.findOne(new Query(criteria), Feedback_V2.class);
        Feedback_V2 feedbackV2 =  mongoTemplate.findById(_id, Feedback_V2.class);
        if (feedbackV2 == null)
            throw new ApiException(HttpStatus.BAD_REQUEST,"Feedback doesn't exist!!");
        return feedbackV2;
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
        long count = mongoTemplate.count(new Query(criteria),Feedback_V2.class);
        return new ApiResponse(200,"List of All feedbacks",feedbackResponse_v2List);
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
        long count = mongoTemplate.count(new Query(criteria),Feedback_V2.class);

        return new ApiResponse(200,"List of All feedbacks",feedbackResponse_v2List);
    }
}
