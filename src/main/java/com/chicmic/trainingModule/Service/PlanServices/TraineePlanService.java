package com.chicmic.trainingModule.Service.PlanServices;

import com.chicmic.trainingModule.Service.FeedBackService.FeedbackService;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.chicmic.trainingModule.TrainingModuleApplication.findTraineeAndMap;
import static com.chicmic.trainingModule.Util.RatingUtil.roundOff_Rating;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;

@Service
public class TraineePlanService {
    private final MongoTemplate mongoTemplate;
    private final FeedbackService feedbackService;

    public TraineePlanService(MongoTemplate mongoTemplate, FeedbackService feedbackService) {
        this.mongoTemplate = mongoTemplate;
        this.feedbackService = feedbackService;
    }

    public List<Document> fetchUserPlans(Integer pageNumber,Integer pageSize,String query,Integer sortDirection,String sortKey){
        //searching!!!
        if(query==null || query.isBlank()) query = ".*";
        int skipValue = (pageNumber - 1) * pageSize;


        java.util.regex.Pattern namePattern = java.util.regex.Pattern.compile(query, java.util.regex.Pattern.CASE_INSENSITIVE);
        //fetching trainee List
        List<Document> userDatasDocuments = findTraineeAndMap().values().stream().map(userDto ->
                        new Document("name",userDto.getName()).append("team",userDto.getTeamName()).append("empCode",userDto.getEmpCode())
                                .append("_id",userDto.get_id()))
                .toList();

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
                                                .append("_id", new Document("$toString",new Document("$arrayElemAt", Arrays.asList("$plans._id", 0)))),
                                        "$$REMOVE"
                                ))
                        ))
                ),
                context -> new Document("$match", new Document("$or", Arrays.asList(
                        new Document("name", new Document("$regex", namePattern)),
                        new Document("team",new Document("$regex",namePattern))// Search by 'team' field, without case-insensitive regex
                ))),
                context -> new Document("$sort", new Document(sortKey, sortDirection)),
                context -> new Document("$skip", Integer.max(skipValue,0)), // Apply skip to paginate
                context -> new Document("$limit", pageSize)
//                context -> new Document("$facet", new Document(
//                        "data", Arrays.asList(
//                        new Document("$sort", new Document(sortKey, sortDirection)),
//                        new Document("$skip", Integer.max(skipValue,0)),
//                        new Document("$limit", pageSize)
//                )
//                ).append("totalCount", Arrays.asList(
//                        new Document("$count", "total")
//                )))
        );
        List<Document>  traineePlanResponseList = mongoTemplate.aggregate(aggregation, "assignTask", Document.class).getMappedResults();
        Set<String> userIds = new HashSet<>();
        Map<String,Integer> userSummary = new HashMap<>();
        int count = 0;
        for (Document document : traineePlanResponseList){
            String _id = (String) document.get("_id");
            userIds.add(_id);
            userSummary.put(_id,count++);
//            UserDto userDto = TrainingModuleApplication.searchUserById(_id);
            document.put("mentor","N/A");
            document.put("rating",0.0f);
        }
        List<Document> traineeRatingSummary = feedbackService.calculateEmployeeRatingSummary(userIds);
//        Map<String,Document> traineeRatingMap = new HashMap<>();
        for (Document document : traineeRatingSummary){
            String _id = (String) document.get("_id");
            int index = userSummary.get(_id);
            traineePlanResponseList.get(index).put("rating",roundOff_Rating((Double)document.get("overallRating")/(int)document.get("count")));
        }
        return traineePlanResponseList;
    }
}
