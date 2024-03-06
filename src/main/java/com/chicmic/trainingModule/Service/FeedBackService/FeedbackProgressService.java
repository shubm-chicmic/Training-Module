package com.chicmic.trainingModule.Service.FeedBackService;

import com.chicmic.trainingModule.Entity.Feedback_V2;
import com.chicmic.trainingModule.Service.CourseServices.CourseService;
import com.chicmic.trainingModule.Service.TestServices.TestService;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.chicmic.trainingModule.Service.FeedBackService.FeedbackService.compute_rating;
import static com.chicmic.trainingModule.Util.TrimNullValidator.FeedbackType.*;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;

@Service
@RequiredArgsConstructor
public class FeedbackProgressService {
    private final MongoTemplate mongoTemplate;
    private final TestService testService;
    private final CourseService courseService;

    public Feedback_V2 feedbackOfParticularPhaseOfTrainee(String traineeId, String planId, String taskId, List<String> subtaskIds, String type, String userId) {
        Criteria criteria = Criteria.where("traineeId").is(traineeId).and("type").is(type);
        criteria.and("planId").is(planId);
        if (type.equals(VIVA_) || type.equals(PPT_)) {
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

    public Map<String, Object> testAggregationQuery(String traineeId, String planId, String taskId, List<String> subtaskIds, String type, String userId) {
        Document matchCriteria1 = new Document("traineeId", traineeId).append("type", type).append("planId", planId).append("isDeleted", false);
        Document matchCriteria2 = new Document("traineeId", traineeId).append("type", type).append("planId", planId).append("isDeleted", false);
        if (type.equals(VIVA_) || type.equals(PPT_)) {
//            criteria.and("details.courseId").is(taskId);
            matchCriteria1.append("details.courseId", taskId);
            matchCriteria2.append("details.courseId", taskId);
            if (type.equals(VIVA_)) {
                matchCriteria1.append("phaseIds", new Document("$size", subtaskIds.size()).append("$in", subtaskIds));
//                matchCriteria1.append("phaseIds", subtaskIds);
                matchCriteria2.append("phaseIds", new Document("$size", subtaskIds.size()).append("$in", subtaskIds));
//                matchCriteria2.append("phaseIds", new Document("$all", subtaskIds));
            }
        } else if (type.equals(TEST_)) {
            matchCriteria1.append("details.testId", taskId);
            matchCriteria1.append("milestoneIds", new Document("$size", subtaskIds.size()).append("$in", subtaskIds));
//            matchCriteria1.append("milestoneIds", subtaskIds);
            matchCriteria2.append("details.testId", taskId);
            matchCriteria2.append("milestoneIds", new Document("$size", subtaskIds.size()).append("$in", subtaskIds));
//            matchCriteria2.append("milestoneIds", new Document("$all", subtaskIds));
        }
        matchCriteria2.append("createdBy", userId);
        Document document = new Document();
        document.append("overallRating", Arrays.asList(new Document("$match", matchCriteria1), new Document("$group", new Document("_id", null)
                .append("totalOverAllRating", new Document("$sum", "$overallRating"))
                .append("count", new Document("$sum", 1)))));
        document.append("feedback", Arrays.asList(new Document("$match", matchCriteria2)));

        Aggregation aggregation = newAggregation(
                context -> new Document("$facet", document)
        );

        Document results = mongoTemplate.aggregate(
                aggregation, "feedback_V2", Document.class
        ).getUniqueMappedResult();
        Map<String, Object> response = new HashMap<>();
        List<Document> traineeOverAllRating = (List<Document>) results.get("overallRating");
        if (traineeOverAllRating == null || traineeOverAllRating.isEmpty()) {
            response.put("overallRating", 0.00);
        } else {
            Double totalOverAllRating = (Double) traineeOverAllRating.get(0).get("totalOverAllRating");
            Integer count = (Integer) traineeOverAllRating.get(0).get("count");
            response.put("overallRating", compute_rating(totalOverAllRating, count));
        }
        List<Document> feedback = (List<Document>) results.get("feedback");
        if (feedback != null && !feedback.isEmpty()) {
            Document feedbackV2 = feedback.get(0);
            response.put("_id", feedbackV2.get("_id"));
        }
        System.out.println(results + "///");
        return response;
    }
}
