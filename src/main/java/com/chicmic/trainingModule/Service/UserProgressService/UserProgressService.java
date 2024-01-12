package com.chicmic.trainingModule.Service.UserProgressService;

import com.chicmic.trainingModule.Dto.UserProgressDto;
import com.chicmic.trainingModule.Entity.*;
import com.chicmic.trainingModule.Entity.Constants.ProgessConstants;
import com.chicmic.trainingModule.Repository.PhaseRepo;
import com.chicmic.trainingModule.Repository.UserProgressRepo;
import com.chicmic.trainingModule.Service.PhaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserProgressService {
    private final UserProgressRepo userProgressRepo;
    private final MongoTemplate mongoTemplate;
    private final PhaseRepo phaseRepo;

    public UserProgress createUserProgress(UserProgress userProgress) {
        System.out.println("Im creating user progress");
        return userProgressRepo.save(userProgress);
    }

    public UserProgress checkUserProgressExists(String planId, String courseId, String subTaskId, String traineeId) {
        List<UserProgress> allUserProgress = mongoTemplate.findAll(UserProgress.class);
        System.out.println("allUserProgress: size " + allUserProgress.size());

        System.out.println("allUserProgress: " + allUserProgress);
        for (UserProgress userProgress : allUserProgress) {

            if(userProgress.getPlanId() != null && userProgress.getCourseId() != null
                    && userProgress.getTraineeId() != null && userProgress.getSubTaskId() != null
            ) {
                if (userProgress.getPlanId().equals(planId) && userProgress.getCourseId().equals(courseId)
                        && userProgress.getTraineeId().equals(traineeId) && userProgress.getSubTaskId().equals(subTaskId)
                ) {
                    return userProgress;
                }
            }

        }
        return  null;
    }
    public UserProgress getUserProgress(UserProgressDto userProgressDto){
        return checkUserProgressExists(
                userProgressDto.getPlanId(),
                userProgressDto.getCourseId(),
                userProgressDto.getSubTaskId(),
                userProgressDto.getTraineeId()
        );
    }

    public UserProgress getUserProgressByTraineeIdAndPlanId(String traineeId, String planId, Integer progressType) {
        return userProgressRepo.findByTraineeIdAndPlanIdAndProgressType(traineeId, planId, progressType).orElse(null);
    }
    public UserProgress getUserProgressByTraineeIdPlanIdAndPlanTaskId(String traineeId, String planId, String planTaskId, Integer progressType) {
        return userProgressRepo.findByTraineeIdAndPlanIdAndPlanTaskIdAndProgressType(traineeId, planId, planTaskId, progressType).orElse(null);
    }
    public UserProgress getUserProgressByTraineeIdPlanIdAndCourseId(String traineeId, String planId, String courseId, Integer progressType) {
        return userProgressRepo.findByTraineeIdAndPlanIdAndCourseIdAndProgressType(traineeId, planId, courseId, progressType).orElse(null);
    }
    public List<UserProgress> getAllUserProgressByTraineeId(String traineeId) {
        return userProgressRepo.findByTraineeId(traineeId);
    }
    public Boolean findIsPlanCompleted(String traineeId, String planId, Integer progressType) {
        UserProgress userProgress = userProgressRepo.findByTraineeIdAndPlanIdAndProgressType(
             traineeId,
                planId,
                progressType
        ).orElse(null);
        if(userProgress == null){
            return false;
        }
        return userProgress.getStatus() == ProgessConstants.Completed;
    }
    public Boolean findIsCourseCompleted(String traineeId, String planId, String courseId, Integer progressType) {
        UserProgress userProgress = userProgressRepo.findByTraineeIdAndPlanIdAndCourseIdAndProgressType(
                traineeId,
                planId,
                courseId,
                progressType
        ).orElse(null);
        if(userProgress == null){
            return false;
        }
        return userProgress.getStatus() == ProgessConstants.Completed;
    }


    public Boolean findIsSubTaskCompleted(String planId, String courseId, String subTaskId, String traineeId) {
        System.out.println("Plan Id : = " + planId);
        System.out.println("Course Id : = " + courseId);
        System.out.println("Trainee Id : = " + traineeId);
        System.out.println("Id : = " + subTaskId);
        Criteria criteria = Criteria.where("traineeId").is(traineeId)
                .and("planId").is(planId)
                .and("courseId").is(courseId)
                .and("subTaskId").is(subTaskId)
                .and("progressType").is(5);


        Query searchQuery = new Query(criteria);
        UserProgress userProgress = mongoTemplate.findOne(searchQuery, UserProgress.class);

        userProgress = checkUserProgressExists(
                planId,
                courseId,
                subTaskId,
                traineeId
        );
        System.out.println("Userprogress dfedf" + userProgress);
//        if(val > 0)return true;
//        return false;
//        UserProgress userProgress = userProgressRepo.findByTraineeIdAndPlanIdAndCourseIdAndIdAndProgressType(
//               userId,
//                planId,
//                courseId,
//                id,
//                5
//        ).orElse(null);
//        UserProgress userProgress =  userProgressRepo.findByPlanIdAndCourseIdAndTraineeIdAndId(
//                planId,
//                courseId,
//                userId,
//               id
//        ).orElse(null);
//        System.out.println("User Progress : = " + userProgress);
        if(userProgress == null){
            return false;
        }
        return userProgress.getStatus() == ProgessConstants.Completed;
    }

//    public long getTotalCompletedTasks(String traineeId, String planType, String id) {
//        Query query = new Query();
//        query.addCriteria(Criteria.where("userId").is(traineeId).and("progressType").is(planType).and("id").is(id));
//        return userProgressRepo.count(query);
//
//    }
    public Integer getTotalSubTaskCompleted(String traineeId,String planId, String courseid, Integer progressType) {
        return Math.toIntExact(userProgressRepo.countByTraineeIdAndPlanIdAndCourseIdAndProgressTypeAndStatus(
                traineeId,
                planId,
                courseid,
                progressType,
                ProgessConstants.Completed
        ));

    }

    public Integer getTotalSubTaskCompletedInPlan(String traineeId, String planId, int progressType) {
        return Math.toIntExact(userProgressRepo.countByTraineeIdAndPlanIdAndProgressTypeAndStatus(
                traineeId,
                planId,
                progressType,
                ProgessConstants.Completed
        ));

    }

    public void deleteUserProgressByPlanId(String userId, String planId) {
        List<UserProgress> userProgressListToDelete = userProgressRepo.findByTraineeIdAndPlanId(userId, planId);
        if (!userProgressListToDelete.isEmpty()) {
            List<String> idsToDelete = userProgressListToDelete.stream()
                    .map(UserProgress::get_id)
                    .collect(Collectors.toList());

            mongoTemplate.findAllAndRemove(Query.query(Criteria.where("_id").in(idsToDelete)), UserProgress.class);
        }
    }

    public void deleteByMilestoneId(String planId, String planTaskId) {
        List<UserProgress> userProgressListToDelete = userProgressRepo.findByPlanIdAndPlanTaskId(planId, planTaskId);
        if (!userProgressListToDelete.isEmpty()) {
            List<String> idsToDelete = userProgressListToDelete.stream()
                    .map(UserProgress::get_id)
                    .collect(Collectors.toList());
            mongoTemplate.findAllAndRemove(Query.query(Criteria.where("_id").in(idsToDelete)), UserProgress.class);

    }
    }
}
