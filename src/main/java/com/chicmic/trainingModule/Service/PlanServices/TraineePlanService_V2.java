package com.chicmic.trainingModule.Service.PlanServices;

import com.chicmic.trainingModule.Dto.ApiResponse.ApiResponse;
import com.chicmic.trainingModule.Dto.UserDto;
import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.chicmic.trainingModule.Dto.UserIdAndStatusDto;
import com.chicmic.trainingModule.Entity.AssignedPlan;
import com.chicmic.trainingModule.Entity.Constants.TrainingStatus;
import com.chicmic.trainingModule.Entity.Filters.Filters;
import com.chicmic.trainingModule.Entity.PlanTask;
import com.chicmic.trainingModule.Service.AssignTaskService.AssignTaskService;
import com.chicmic.trainingModule.Service.FeedBackService.FeedbackService;
import com.chicmic.trainingModule.Service.TraineeService;
import com.chicmic.trainingModule.TrainingModuleApplication;
import com.chicmic.trainingModule.Util.DateTimeUtil;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

import static com.chicmic.trainingModule.Service.FeedBackService.FeedbackService.compute_rating;
import static com.chicmic.trainingModule.TrainingModuleApplication.findTraineeAndMap;
import static com.chicmic.trainingModule.TrainingModuleApplication.searchNameById;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;

@Service
public class TraineePlanService_V2 {
    private final MongoTemplate mongoTemplate;
    private final FeedbackService feedbackService;
    private final AssignTaskService assignTaskService;

    public TraineePlanService_V2(MongoTemplate mongoTemplate, FeedbackService feedbackService, AssignTaskService assignTaskService) {
        this.mongoTemplate = mongoTemplate;
        this.feedbackService = feedbackService;
        this.assignTaskService = assignTaskService;
    }

    public ApiResponse fetchUserPlans(Integer pageNumber, Integer pageSize, String query, Integer sortDirection, String sortKey, String currentUserId, Filters filters){
        System.out.println("dsbvmdsbvbnsd....................");
        //searching!!!

        if(query==null || query.isBlank()) query = ".*";
        int skipValue = pageNumber;//(pageNumber - 1) * pageSize;


        //query1.fields().include("plans._id")
        List<AssignedPlan> assignedPlanList = mongoTemplate.find(new Query(),AssignedPlan.class);
        if (assignedPlanList.size() == 0){
            mongoTemplate.insert(AssignedPlan.builder().userId("12345").date(LocalDateTime.now()).deleted(true),"assignedPlan");
        }

        java.util.regex.Pattern namePattern = java.util.regex.Pattern.compile(query, java.util.regex.Pattern.CASE_INSENSITIVE);

        // Fetching trainee List
        List<Document> userDatasDocuments = new ArrayList<>();
        Map<String, UserDto> traineeMap = findTraineeAndMap();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Boolean isIndividualRole = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("IND"));
        for (UserDto userDto : traineeMap.values()) {
            Set<String> userTeams = new HashSet<>(userDto.getTeams());
            Set<String> teamsFilter = filters.getTeamsFilter();
            boolean teamsFilterNullOrEmpty = teamsFilter == null || teamsFilter.isEmpty();
            boolean hasCommonTeam = teamsFilterNullOrEmpty || !Collections.disjoint(userTeams, teamsFilter);

            if(hasCommonTeam) {
                if (userDto.get_id().equals(currentUserId)) {
                    Document document = new Document();
                    document.append("name", userDto.getName())
                            .append("team", userDto.getTeamName())
                            .append("empCode", userDto.getEmpCode())
                            .append("_id", userDto.get_id());
                    userDatasDocuments.add(document);
                } else if (isIndividualRole) {
                    if ((assignTaskService.isUserMentorOfTrainee(userDto.get_id(), currentUserId) || TraineeService.isUserInSameTeam(userDto, TrainingModuleApplication.idUserMap.get(currentUserId)))) {
                        Document document = new Document();
                        document.append("name", userDto.getName())
                                .append("team", userDto.getTeamName())
                                .append("empCode", userDto.getEmpCode())
                                .append("_id", userDto.get_id());
                        userDatasDocuments.add(document);
                    }
                } else {
                    Document document = new Document();
                    document.append("name", userDto.getName())
                            .append("team", userDto.getTeamName())
                            .append("empCode", userDto.getEmpCode())
                            .append("_id", userDto.get_id());
                    userDatasDocuments.add(document);
                }
            }
        }


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
                        .append("status", new Document("$first", "$$ROOT.trainingStatus"))
                        .append("startDate",new Document("$first","$$ROOT.date"))// Include the "deleted" field
                ),
                context -> new Document("$facet",new Document("data",Arrays.asList(
                        new Document("$match", new Document("$or", Arrays.asList(
                                new Document("name", new Document("$regex", namePattern)),
                                new Document("team", new Document("$regex", namePattern)) // Search by 'team' field, without case-insensitive regex
                        ))),
                        new Document("$sort", new Document(sortKey, sortDirection)),
                        new Document("$skip", Integer.max(skipValue, 0)), // Apply skip to paginate
                        new Document("$limit", pageSize)
                ))
                        .append("total",Arrays.asList(
                                new Document("$match", new Document("$or", Arrays.asList(
                                        new Document("name", new Document("$regex", namePattern)),
                                        new Document("team", new Document("$regex", namePattern)) // Search by 'team' field, without case-insensitive regex
                                ))),
                                new Document("$count", "total")
                        ))
                )
//
//                context -> new Document("$project", new Document().append("status",1).append("date",1)),
//                context -> new Document("$match", new Document("$or", Arrays.asList(
//                        new Document("name", new Document("$regex", namePattern)),
//                        new Document("team",new Document("$regex",namePattern))// Search by 'team' field, without case-insensitive regex
//                ))),
//                context -> new Document("$sort", new Document(sortKey, sortDirection)),
//                context -> new Document("$skip", Integer.max(skipValue,0)), // Apply skip to paginate
//                context -> new Document("$limit", pageSize)
        );
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        Document  response = mongoTemplate.aggregate(aggregation, "assignedPlan", Document.class).getUniqueMappedResult();
        System.out.println("\u001B[34m" + response.get("total") + "\u001B[0m");
        List<Document>  traineePlanResponseList = (List<Document>) response.get("data");
        List<Document> countList = (List<Document>) response.get("total");
        int cnt = countList.isEmpty() ? 0 : (Integer) countList.get(0).get("total");
        System.out.println(cnt + "--------------");
        for (Document tr : traineePlanResponseList){
            List<UserIdAndNameDto> planDetails = new ArrayList<>();
            HashSet<String> names = new HashSet<>();
            assignedPlanList.forEach(ap -> {
                if(ap.getUserId().equals(tr.get("_id")))
                    ap.getPlans().forEach(p-> {
                                if (p!= null)
                                    planDetails.add(new UserIdAndNameDto(p.get_id(), p.getPlanName()));
                                if(p!= null && !p.getDeleted() && p.getPhases()!=null) {
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
            List<UserIdAndNameDto> mentorNames = new ArrayList<>();

// Add mentors to the List
            names.forEach(nm -> mentorNames.add(new UserIdAndNameDto(nm, searchNameById(nm))));

// Sort the mentorNames List by user names
            mentorNames.sort(Comparator.comparing(UserIdAndNameDto::getName));

            System.out.println("\u001B[35m Mentor names");
            System.out.println(mentorNames);
            System.out.println("\u001B[0m");

            tr.put("mentor", mentorNames);
            tr.put("plan",planDetails);
            System.out.println(tr.get("status") + "}}}}}}}}}}}}}}");
            System.out.println(tr.get("startDate") + "}}}}}}}}}}}}}}");
            String userId = (String) tr.get("_id");
            AssignedPlan assignedPlan = assignTaskService.getAllAssignTasksByTraineeId(userId);
            if(assignedPlan != null){
                tr.put("status", assignedPlan.getTrainingStatus());
                tr.put("startDate", formatter.format(DateTimeUtil.convertLocalDateTimeToDate(assignedPlan.getDate())));
            }else {
                tr.put("status", TrainingStatus.PENDING);
                tr.put("startDate",formatter.format(DateTimeUtil.convertLocalDateTimeToDate(LocalDateTime.now())));
            }
        };

        Set<String> userIds = new HashSet<>();
        Map<String,Integer> userSummary = new HashMap<>();
        int count = 0;
        for (Document document : traineePlanResponseList){
            String _id = (String) document.get("_id");

            userIds.add(_id);
            userSummary.put(_id,count++);
            UserDto userDto = traineeMap.get(_id);
            if (userDto != null) {
                List<String> teams = userDto.getTeams();
                List<UserIdAndNameDto> teamsIdNames = new ArrayList<>();
                for (String teamId : teams) {
                    teamsIdNames.add(UserIdAndNameDto.builder()
                                    ._id(teamId)
                                    .name(TrainingModuleApplication.teamIdAndNameMap.get(teamId))
                                    .build());
                }
                document.put("teams", teamsIdNames);

            }
            document.put("rating",0.0f);
        }
        List<Document> traineeRatingSummary = feedbackService.calculateEmployeeRatingSummary(userIds);
//        Map<String,Document> traineeRatingMap = new HashMap<>();
        for (Document document : traineeRatingSummary){
            String _id = (String) document.get("_id");
            int index = userSummary.get(_id);
            traineePlanResponseList.get(index).put("rating",compute_rating((Double)document.get("overallRating"),(int)document.get("count")));
        }
        return new ApiResponse(200,"Plan fetched successfully to user",traineePlanResponseList, Long.valueOf(cnt));
//        return traineePlanResponseList;
    }

    public void updateTraineeStatus(UserIdAndStatusDto userIdAndStatusDto, String createdBy){
        Criteria criteria = Criteria.where("userId").is(userIdAndStatusDto.getTraineeId());
        Update update = new Update();
        update.set("trainingStatus",userIdAndStatusDto.getStatus());
        UpdateResult updateResult = mongoTemplate.updateFirst(new Query(criteria),update,AssignedPlan.class);
        if (updateResult.getModifiedCount() == 0)
        {
            AssignedPlan assignedPlan = AssignedPlan.builder()
                    .plans(new ArrayList<>())
                    .date(LocalDateTime.now())
                    .trainingStatus(userIdAndStatusDto.getStatus())
                    .userId(userIdAndStatusDto.getTraineeId())
                    .updatedAt(LocalDateTime.now())
                    .createdAt(LocalDateTime.now())
                    .createdBy(createdBy)
                    .deleted(false)
                    .approved(false)
                    .build();
            assignTaskService.saveAssignTask(assignedPlan);
        }
    }
}
