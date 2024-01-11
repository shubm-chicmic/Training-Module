package com.chicmic.trainingModule.Service.PlanServices;

import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.chicmic.trainingModule.Entity.AssignedPlan;
import com.chicmic.trainingModule.Entity.PlanTask;
import com.chicmic.trainingModule.Service.FeedBackService.FeedbackService_V2;
import com.chicmic.trainingModule.TrainingModuleApplication;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

import static com.chicmic.trainingModule.TrainingModuleApplication.findTraineeAndMap;
import static com.chicmic.trainingModule.TrainingModuleApplication.searchNameById;
import static com.chicmic.trainingModule.Util.RatingUtil.roundOff_Rating;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newUpdate;

@Service
public class TraineePlanService_V2 {
    private final MongoTemplate mongoTemplate;
    private final FeedbackService_V2 feedbackService;

    public TraineePlanService_V2(MongoTemplate mongoTemplate, FeedbackService_V2 feedbackService) {
        this.mongoTemplate = mongoTemplate;
        this.feedbackService = feedbackService;
    }
    public List<Document> fetchUserPlans(Integer pageNumber, Integer pageSize, String query, Integer sortDirection, String sortKey){
        System.out.println("dsbvmdsbvbnsd....................");
        //searching!!!
        if(query==null || query.isBlank()) query = ".*";
        int skipValue = (pageNumber - 1) * pageSize;


        //query1.fields().include("plans._id")
        List<AssignedPlan> assignedPlanList = mongoTemplate.find(new Query(),AssignedPlan.class);
        if (assignedPlanList.size() == 0){
            mongoTemplate.insert(AssignedPlan.builder().userId("12345").date(LocalDateTime.now()).deleted(true),"assignedPlan");
        }

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
        );

//        Aggregation aggregation = newAggregation(
//                context -> new Document("$addFields", new Document("userDatas",
//                        userDatasDocuments
//                )),
//                context -> new Document("$unwind", new Document("path", "$userDatas").append("preserveNullAndEmptyArrays", true)),
//                context -> new Document("$lookup",new Document("from", "plan")
//                        .append("localField", "plans.$id")
//                        .append("foreignField", "_id")
//                        .append("as", "planDetails")
//                ),
//                context -> new Document("$group", new Document("_id", "$userDatas._id")
//                        .append("name", new Document("$first", "$userDatas.name"))
//                        .append("team", new Document("$first", "$userDatas.team"))
//                        .append("employeeCode", new Document("$first", "$userDatas.empCode"))
//                        .append("plan", new Document("$addToSet",
//                                new Document("$cond", Arrays.asList(
//                                        new Document("$eq", Arrays.asList("$userId", "$userDatas._id")),
//                                        new Document("name", new Document("$arrayElemAt", Arrays.asList("$planDetails.planName", 0)))
//                                                .append("_id", new Document("$toString",new Document("$arrayElemAt", Arrays.asList("$planDetails._id", 0)))),
//                                        "$$REMOVE"
//                                ))
//                        ))
//                ),
//                context -> new Document("$match", new Document("$or", Arrays.asList(
//                        new Document("name", new Document("$regex", namePattern)),
//                        new Document("team",new Document("$regex",namePattern))// Search by 'team' field, without case-insensitive regex
//                ))),
//                context -> new Document("$sort", new Document(sortKey, sortDirection)),
//                context -> new Document("$skip", Integer.max(skipValue,0)), // Apply skip to paginate
//                context -> new Document("$limit", pageSize)
//        );

        List<Document>  traineePlanResponseList = mongoTemplate.aggregate(aggregation, "assignedPlan", Document.class).getMappedResults();

        for (Document tr : traineePlanResponseList){
            List<UserIdAndNameDto> planDetails = new ArrayList<>();
            HashSet<String> names = new HashSet<>();
            assignedPlanList.forEach(ap -> {
                if(ap.getUserId().equals(tr.get("_id")))
                    ap.getPlans().forEach(p-> {
                                if (p!= null)
                                    planDetails.add(new UserIdAndNameDto(p.get_id(), p.getPlanName()));
                                if(p!= null && p.getPhases()!=null) {
                                    p.getPhases().forEach(ph -> {
                                        ph.getTasks().forEach(pt ->{
                                            if(pt!=null && pt instanceof PlanTask)
                                                names.addAll(pt.getMentorIds());
                                        });
                                    });
                                }
                            }
                    );
            });
            HashSet<String> mentorNames = new HashSet<>();
            names.forEach(nm-> mentorNames.add(searchNameById(nm)));
            tr.put("mentor",mentorNames);
            tr.put("plan",planDetails);
        };

        Set<String> userIds = new HashSet<>();
        Map<String,Integer> userSummary = new HashMap<>();
        int count = 0;
        for (Document document : traineePlanResponseList){
            String _id = (String) document.get("_id");
            userIds.add(_id);
            userSummary.put(_id,count++);
//            UserDto userDto = TrainingModuleApplication.searchUserById(_id);
//            document.put("mentor","Rohit");
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
