package com.chicmic.trainingModule.Service.FeedBackService;

import com.chicmic.trainingModule.Entity.Feedback_V2;
import com.chicmic.trainingModule.Service.CourseServices.CourseService;
import com.chicmic.trainingModule.Service.TestServices.TestService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.chicmic.trainingModule.Service.FeedBackService.FeedbackService_V2.compute_rating;
import static com.chicmic.trainingModule.Util.TrimNullValidator.FeedbackType.*;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;

@Service
@RequiredArgsConstructor
public class FeedbackProgressService {
    private final MongoTemplate mongoTemplate;
    private final TestService testService;
    private final CourseService courseService;
    public Feedback_V2 feedbackOfParticularPhaseOfTrainee(String traineeId, String planId, String taskId, List<String> subtaskIds, String type, String userId){
        Criteria criteria = Criteria.where("traineeId").is(traineeId).and("type").is(type);
        criteria.and("planId").is(planId);
        if (type.equals(VIVA_)||type.equals(PPT_)) {
            criteria.and("details.courseId").is(taskId);
            if (type.equals(VIVA_))
                criteria.and("phaseIds").all(subtaskIds);
        } else if (type.equals(TEST_)) {
            criteria.and("details.testId").is(taskId);
            criteria.and("milestoneIds").all(subtaskIds);
        }
        criteria.and("createdBy").is(userId);
        return mongoTemplate.findOne(new Query(criteria), Feedback_V2.class);
    }
}
