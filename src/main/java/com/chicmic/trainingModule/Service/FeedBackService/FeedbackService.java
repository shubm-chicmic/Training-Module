package com.chicmic.trainingModule.Service.FeedBackService;

import com.chicmic.trainingModule.Dto.FeedBackDto;
import com.chicmic.trainingModule.Dto.ratings.Rating;
import com.chicmic.trainingModule.Entity.Feedback;
import com.chicmic.trainingModule.ExceptionHandling.ApiException;
import com.chicmic.trainingModule.Repository.FeedbackRepo;
import com.mongodb.client.result.DeleteResult;
import jdk.jshell.execution.Util;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Date;

import static com.chicmic.trainingModule.Util.FeedbackUtil.FEEDBACK_TYPE_CATEGORY;

@Service
public class FeedbackService {
    private final FeedbackRepo feedbackRepo;
    private final MongoTemplate mongoTemplate;

    public FeedbackService(FeedbackRepo feedbackRepo, MongoTemplate mongoTemplate) {
        this.feedbackRepo = feedbackRepo;
        this.mongoTemplate = mongoTemplate;
    }

    public Feedback saveFeedbackInDB(FeedBackDto feedBackDto, String userId){
        Rating rating = Rating.getRating(feedBackDto);
        System.out.println(rating.getClass()+"}}}}}}}}}}}}}}}}}}}}}}}}}}");
        Date currentDate = new Date(System.currentTimeMillis());

        Feedback feedback = Feedback.builder()
                .traineeID(feedBackDto.getTraineeId())
                .rating(rating)
                .feedbackType(feedBackDto.getFeedBackTypeId())
                .message(feedBackDto.getMessage())
                .createdAt(currentDate)
                .updateAt(currentDate)
                .createdBy(userId)
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

        Criteria criteria = Criteria.where("id").is(_id).and("createdBy").is(userId);
        Query query = new Query(criteria);
        Update update = new Update()
                .set("updateAt",new Date(System.currentTimeMillis()))
                .set("message",feedBackDto.getMessage())
                .set("traineeID",feedBackDto.getTraineeId())
                .set("feedbackType",feedBackDto.getFeedBackTypeId())
                .set("rating",rating);

        FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true);

        return mongoTemplate.findAndModify(query,update,options,Feedback.class);
    }
}
