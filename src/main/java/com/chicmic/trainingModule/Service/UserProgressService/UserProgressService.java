package com.chicmic.trainingModule.Service.UserProgressService;

import com.chicmic.trainingModule.Dto.UserProgressDto;
import com.chicmic.trainingModule.Entity.Constants.ProgessConstants;
import com.chicmic.trainingModule.Entity.Course;
import com.chicmic.trainingModule.Entity.UserProgress;
import com.chicmic.trainingModule.Repository.UserProgressRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserProgressService {
    private final UserProgressRepo userProgressRepo;
    private final MongoTemplate mongoTemplate;

    public UserProgress createUserProgress(UserProgress userProgress) {
        return userProgressRepo.save(userProgress);
    }
    public UserProgress getUserProgress(UserProgressDto userProgressDto){
        return userProgressRepo.findByPlanIdAndCourseIdAndTraineeIdAndId(
                userProgressDto.getPlanId(),
                userProgressDto.getCourseId(),
                userProgressDto.getTraineeId(),
                userProgressDto.getId()
        ).orElse(null);
    }

    public Boolean findIsPlanCompleted(String planId, String courseId, Integer planType, String userId) {
        UserProgress userProgress = userProgressRepo.findByTraineeIdAndPlanIdAndCourseIdAndProgressType(
                planId,
               courseId,
                userId,
                planType
        ).orElse(null);
        if(userProgress == null){
            return false;
        }
        return userProgress.getStatus() == ProgessConstants.Completed;
    }

    public Boolean findIsSubTaskCompleted(String planId, String courseId, String id, String traineeId) {
        System.out.println("Plan Id : = " + planId);
        System.out.println("Course Id : = " + courseId);
        System.out.println("Trainee Id : = " + traineeId);
        System.out.println("Id : = " + id);
        Criteria criteria = Criteria.where("traineeId").is(traineeId)
                .and("planId").is(planId)
                .and("courseId").is(courseId)
                .and("id").is(id)
                .and("progressType").is(5);


        Query searchQuery = new Query(criteria);
        UserProgress userProgress = mongoTemplate.findOne(searchQuery, UserProgress.class);
        System.out.println("Userprogress " + userProgress);
//        if(val > 0)return true;
        return false;
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
//        if(userProgress == null){
//            return false;
//        }
//        return userProgress.getStatus() == ProgessConstants.Completed;
    }

//    public long getTotalCompletedTasks(String traineeId, String planType, String id) {
//        Query query = new Query();
//        query.addCriteria(Criteria.where("userId").is(traineeId).and("progressType").is(planType).and("id").is(id));
//        return userProgressRepo.count(query);
//
//    }
    public Integer getTotalSubTaskCompleted(String traineeId,String planId, String courseid, Integer progressType) {
        return Math.toIntExact(userProgressRepo.countByTraineeIdAndPlanIdAndCourseIdAndProgressType(
                traineeId,
                planId,
                courseid,
                progressType
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
}
