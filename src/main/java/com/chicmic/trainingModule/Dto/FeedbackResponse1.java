package com.chicmic.trainingModule.Dto;

import com.chicmic.trainingModule.Dto.ratings.Rating_BEHAVIOUR;
import com.chicmic.trainingModule.Dto.ratings.Rating_COURSE;
import com.chicmic.trainingModule.Dto.ratings.Rating_PPT;
import com.chicmic.trainingModule.Dto.ratings.Rating_TEST;
import com.chicmic.trainingModule.Entity.Feedback;
import com.chicmic.trainingModule.TrainingModuleApplication;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import static com.chicmic.trainingModule.TrainingModuleApplication.searchUserById;

@Getter @Setter @Builder
public class FeedbackResponse1 {
    private Trainee trainee;
    private Trainee reviewer;
    private int feedbackType;
    private Course course;
    private Phase phase;
    private Test test;
    private Milestone milestone;
    private float communicationRating;
    private float presentationRating;
    private float technicalRating;
    private float theoreticalRating;
    private float codingRating;
    private float attitudeRating;
    private float teamSpiritRating;
    private String comment;

    public static FeedbackResponse1 buildResponse(Feedback feedback){
        //Rating_PPT rating_ppt = (Rating_PPT) feedback.getRating();
        UserDto userDto = searchUserById(feedback.getTraineeID());

        int feedbackTypeId = feedback.getFeedbackType().charAt(0) - '1';

         FeedbackResponse1 feedbackResponse1 = FeedbackResponse1.builder()
                 .feedbackType((feedback.getFeedbackType().charAt(0) - '0'))
                         .trainee(new Trainee(feedback.getTraineeID(),userDto.getName()))
                 .reviewer(new Trainee(feedback.getCreatedBy(), TrainingModuleApplication.searchNameById(feedback.getCreatedBy())))
                 .comment(feedback.getComment())
                 .build();

         if(feedbackTypeId == 0){
             Rating_COURSE rating_course = (Rating_COURSE)  feedback.getRating();
            feedbackResponse1.setCourse(new Course(rating_course.getCourseId(), rating_course.getCourseId()));
            feedbackResponse1.setPhase(new Phase(rating_course.getPhaseId(),rating_course.getPhaseId()));
            feedbackResponse1.setTheoreticalRating(rating_course.getTheoreticalRating());
            feedbackResponse1.setTechnicalRating(rating_course.getTechnicalRating());
            feedbackResponse1.setCommunicationRating(rating_course.getCommunicationRating());
         }else if(feedbackTypeId == 1){
             Rating_TEST rating_test = (Rating_TEST) feedback.getRating();
             feedbackResponse1.setTest(new Test(rating_test.getTestId(), rating_test.getTestId()));
             feedbackResponse1.setMilestone(new Milestone(rating_test.getMilestoneId(),rating_test.getMilestoneId()));
             feedbackResponse1.setTheoreticalRating(rating_test.getTheoreticalRating());
             feedbackResponse1.setCommunicationRating(rating_test.getCommunicationRating());
             feedbackResponse1.setCommunicationRating(rating_test.getCommunicationRating());
         }else if (feedbackTypeId == 2){
             Rating_PPT rating_ppt = (Rating_PPT) feedback.getRating();
             feedbackResponse1.setCommunicationRating(rating_ppt.getCommunicationRating());
             feedbackResponse1.setTechnicalRating(rating_ppt.getTechnicalRating());
             feedbackResponse1.setPresentationRating(rating_ppt.getPresentationRating());
         }else{
             Rating_BEHAVIOUR rating_behaviour = (Rating_BEHAVIOUR) feedback.getRating();
             feedbackResponse1.setAttitudeRating(rating_behaviour.getAttitudeRating());
             feedbackResponse1.setTeamSpiritRating(rating_behaviour.getTeamSpiritRating());
         }
         return feedbackResponse1;
    }
}
