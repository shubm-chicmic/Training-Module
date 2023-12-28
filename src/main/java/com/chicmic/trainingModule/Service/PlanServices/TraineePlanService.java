package com.chicmic.trainingModule.Service.PlanServices;

import com.chicmic.trainingModule.Dto.PlanDto.PlanRequestDto;
import com.chicmic.trainingModule.Dto.TraineePlanReponse;
import com.chicmic.trainingModule.Dto.UserDto;
import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.chicmic.trainingModule.Entity.Plan.UserPlan;
import com.chicmic.trainingModule.Repository.UserPlanRepo;
import com.chicmic.trainingModule.Service.FeedBackService.FeedbackService;
import com.chicmic.trainingModule.TrainingModuleApplication;
import org.bson.Document;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.chicmic.trainingModule.TrainingModuleApplication.findTraineeAndMap;
import static com.chicmic.trainingModule.Util.FeedbackUtil.searchNameAndEmployeeCode;
import static com.chicmic.trainingModule.Util.RatingUtil.roundOff_Rating;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;

@Service
public class TraineePlanService {
    private final MongoTemplate mongoTemplate;
    private final UserPlanRepo userPlanRepo;
    private final FeedbackService feedbackService;
    private final PlanService planService;

    public TraineePlanService(MongoTemplate mongoTemplate, UserPlanRepo userPlanRepo, FeedbackService feedbackService, PlanService planService) {
        this.mongoTemplate = mongoTemplate;
        this.userPlanRepo = userPlanRepo;
        this.feedbackService = feedbackService;
        this.planService = planService;
    }

    public List<TraineePlanReponse> assignMultiplePlansToTrainees(PlanRequestDto planRequestDto, String createdBy){
//        AssignTaskDto assignTaskDto = new AssignTaskDto();
//        PlanRequestDto.builder().trainees(new HashSet<>( assignTaskDto.getUsers())).planId(assignTaskDto.getPlanIds().get(0))
//                .reviewers(assignTaskDto.getReviewers());
        List<UserPlan> userPlans = new ArrayList<>();
        for (String traineeId : planRequestDto.getTrainees()){
            TrainingModuleApplication.searchUserById(traineeId);

            UserPlan userPlan = UserPlan.builder()
                    .reviewerId(planRequestDto.getReviewers())
                    .traineeId(traineeId)
                    ._id(traineeId)
                    .createdBy(createdBy)
                    .createdAt(new Date(System.currentTimeMillis()))
                    .updatedAt(new Date(System.currentTimeMillis()))
                    .planId(planRequestDto.getPlanId())
                    .build();
            userPlans.add(userPlan);
        }
        userPlanRepo.saveAll(userPlans);
        //fetch user plans!!
        List<UserPlan> userDtos = mongoTemplate.find(new Query().with(PageRequest.of(0,10)), UserPlan.class);
        //fetch course name through plan----
        HashMap<String,String> userPlanId = new HashMap<>();
        Set<String> userIds = userDtos.stream().map((user)->{
            userPlanId.put(user.getTraineeId(),user.getPlanId());
            return user.getTraineeId();
        }).collect(Collectors.toSet());
        List<String> planIds = userDtos.stream().map(UserPlan::getPlanId).collect(Collectors.toList());
        List<Document> documentList = feedbackService.calculateEmployeeRatingSummary(userIds);
        List<TraineePlanReponse> traineePlanReponseList = new ArrayList<>();
        HashMap<String, List<UserIdAndNameDto>> planDetails = planService.getPlanCourseByPlanIds(planIds);
        for (Document document : documentList){
            String _id = (String) document.get("_id");
            UserDto userDto = TrainingModuleApplication.searchUserById(_id);
            traineePlanReponseList.add(TraineePlanReponse.builder()
                            .team(userDto.getTeamName())
                            .mentor("N/A")
                            .name(userDto.getName())
                            .plan(planDetails.get(userPlanId.get(_id)))
                            .employeeCode(userDto.getEmpCode())
                            .rating(roundOff_Rating((Double)document.get("overallRating")/(int)document.get("count")))
                            ._id(_id)
                    .build());
        }
        return traineePlanReponseList;
    }

    public List<Document> fetchUserPlans(Integer pageNumber,Integer pageSize,String query,Integer sortDirection,String sortKey){
        //searching!!!
        if(query==null || query.isBlank()) query = ".*";
        int skipValue = (pageNumber - 1) * pageSize;

        sortDirection = (sortDirection!=1)?-1:1;

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
                                                .append("_id", new Document("$arrayElemAt", Arrays.asList("$plans._id", 0))),
                                        "$$REMOVE"
                                ))
                        ))
                ),
                context -> new Document("$match", new Document("$or", Arrays.asList(
                        new Document("name", new Document("$regex", namePattern)),
                        new Document("team",new Document("$regex",namePattern))// Search by 'team' field, without case-insensitive regex
                ))),
                context -> new Document("$skip", Integer.max(skipValue,0)), // Apply skip to paginate
                context -> new Document("$limit", pageSize)
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
