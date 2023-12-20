package com.chicmic.trainingModule.Service.PlanServices;

import com.chicmic.trainingModule.Dto.PlanDto.PlanRequestDto;
import com.chicmic.trainingModule.Dto.TraineePlanReponse;
import com.chicmic.trainingModule.Dto.UserDto;
import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.chicmic.trainingModule.Entity.Plan.UserPlan;
import com.chicmic.trainingModule.Repository.UserPlanRepo;
import com.chicmic.trainingModule.Service.FeedBackService.FeedbackService;
import com.chicmic.trainingModule.TrainingModuleApplication;
import com.chicmic.trainingModule.Util.FeedbackUtil;
import org.bson.Document;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

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
        List<String> userIds = userDtos.stream().map((user)->{
            userPlanId.put(user.getTraineeId(),user.getPlanId());
            return user.getTraineeId();
        }).collect(Collectors.toList());
        List<String> planIds = userDtos.stream().map(UserPlan::getPlanId).collect(Collectors.toList());
        List<Document> documentList = feedbackService.calculateEmployeeRatingSummary(userIds);
        List<TraineePlanReponse> traineePlanReponseList = new ArrayList<>();
        HashMap<String, List<UserIdAndNameDto>> planDetails = planService.getPlanCourseByPlanIds(planIds);
        for (Document document : documentList){
            String _id = (String) document.get("_id");
            UserDto userDto = TrainingModuleApplication.searchUserById(_id);
            traineePlanReponseList.add(TraineePlanReponse.builder()
                            .team(new UserIdAndNameDto(userDto.getTeamId(),userDto.getTeamName()))
                            .name(userDto.getName())
                            .course(planDetails.get(userPlanId.get(_id)))
                            .overallRating(roundOff_Rating((Double)document.get("overallRating")/(int)document.get("count")))
                            ._id(_id)
                    .build());
        }
        return traineePlanReponseList;
    }
}
