package com.chicmic.trainingModule.Service.PlanServices;

import com.chicmic.trainingModule.Dto.AssignTaskDto.AssignTaskDto;
import com.chicmic.trainingModule.Dto.PlanDto.PlanRequestDto;
import com.chicmic.trainingModule.Dto.TraineePlanReponse;
import com.chicmic.trainingModule.Dto.UserDto;
import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.chicmic.trainingModule.Entity.Plan.UserPlan;
import com.chicmic.trainingModule.Repository.UserPlanRepo;
import com.chicmic.trainingModule.Service.FeedBackService.FeedbackService;
import com.chicmic.trainingModule.TrainingModuleApplication;
import org.apache.catalina.User;
import org.bson.Document;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.chicmic.trainingModule.Util.FeedbackUtil.searchNameAndEmployeeCode;
import static com.chicmic.trainingModule.Util.RatingUtil.roundOff_Rating;

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
                            .course(planDetails.get(userPlanId.get(_id)))
                            .employeeCode(userDto.getEmpCode())
                            .rating(roundOff_Rating((Double)document.get("overallRating")/(int)document.get("count")))
                            ._id(_id)
                    .build());
        }
        return traineePlanReponseList;
    }
    public List<TraineePlanReponse> fetchUserPlans(Integer pageNumber,Integer pageSize,String searchString,Integer sortDirection,String sortKey){
        Pageable pageable;
        if (!sortKey.isEmpty()) {
            Sort.Direction direction = (sortDirection == 1) ? Sort.Direction.ASC : Sort.Direction.DESC;
            Sort sort = Sort.by(direction, sortKey);
            pageable = PageRequest.of(pageNumber, pageSize, sort);
        } else {
            pageable = PageRequest.of(pageNumber, pageSize);
        }
        Query query1 = new Query();
        //search query!!
        if(searchString!=null && !searchString.isBlank()) {
            query1 = new Query(Criteria.where("traineeId").in(searchNameAndEmployeeCode(searchString)));
            //criteria.and("traineeId").in(searchNameAndEmployeeCode(query));
        }

        List<UserPlan> userPlanList = mongoTemplate.find(query1.with(pageable), UserPlan.class);
        HashMap<String,String> userPlanId = new HashMap<>();
        Set<String> userIds = userPlanList.stream().map((user)->{
            userPlanId.put(user.getTraineeId(),user.getPlanId());
            return user.getTraineeId();
        }).collect(Collectors.toSet());

        List<String> planIds = userPlanList.stream().map(UserPlan::getPlanId).collect(Collectors.toList());
        List<Document> documentList = feedbackService.calculateEmployeeRatingSummary(userIds);
        List<TraineePlanReponse> traineePlanReponseList = new ArrayList<>();
        HashMap<String, List<UserIdAndNameDto>> planDetails = planService.getPlanCourseByPlanIds(planIds);

//        if(documentList.size()==0){
//            for (UserPlan userPlan : userPlanList){
//                String _id = userPlan.get_id();
//                UserDto userDto = TrainingModuleApplication.searchUserById(_id);
//                traineePlanReponseList.add(TraineePlanReponse.builder()
//                        .team(userDto.getTeamName())
//                        .mentor("N/A")
//                        .name(userDto.getName())
//                        .course(planDetails.get(userPlanId.get(_id)))
//                        .employeeCode(userDto.getEmpCode())
//                        .rating(0f)
//                        ._id(_id)
//                        .build());
//            }
//        }

        //few id's are present and few are not!!
        for (Document document : documentList){
            String _id = (String) document.get("_id");
            UserDto userDto = TrainingModuleApplication.searchUserById(_id);
            userIds.remove(_id);
            traineePlanReponseList.add(TraineePlanReponse.builder()
                    .team(userDto.getTeamName())
                    .mentor("safafa")
                    .name(userDto.getName())
                    .course(planDetails.get(userPlanId.get(_id)))
                    .employeeCode(userDto.getEmpCode())
                    .rating(roundOff_Rating((Double)document.get("overallRating")/(int)document.get("count")))
                    ._id(_id)
                    .build());
        }
        for (String _id : userIds){
            UserDto userDto = TrainingModuleApplication.searchUserById(_id);
            traineePlanReponseList.add(TraineePlanReponse.builder()
                    .team(userDto.getTeamName())
                    .mentor("N/A")
                    .name(userDto.getName())
                    .course(planDetails.get(userPlanId.get(_id)))
                    .employeeCode(userDto.getEmpCode())
                    .rating(0f)
                    ._id(_id)
                    .build());
        }
        return traineePlanReponseList;
    }
}
