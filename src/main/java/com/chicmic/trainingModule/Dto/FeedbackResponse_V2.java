package com.chicmic.trainingModule.Dto;

import com.chicmic.trainingModule.Dto.rating.Rating_BEHAVIOUR;
import com.chicmic.trainingModule.Dto.rating.Rating_COURSE;
import com.chicmic.trainingModule.Dto.rating.Rating_PPT;
import com.chicmic.trainingModule.Dto.rating.Rating_TEST;
import com.chicmic.trainingModule.Entity.Feedback_V2;
import com.chicmic.trainingModule.TrainingModuleApplication;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

import static com.chicmic.trainingModule.TrainingModuleApplication.searchUserById;
import static com.chicmic.trainingModule.Util.TrimNullValidator.FeedbackType.*;

@Getter
@Setter
@Builder
public class FeedbackResponse_V2 {
    private Trainee trainee;
    private Trainee reviewer;
    private int feedbackType;
    private UserIdAndNameDto course;
    private List<UserIdAndNameDto> phase;
    private UserIdAndNameDto test;
    private List<UserIdAndNameDto> milestone;
    private float communicationRating;
    private float presentationRating;
    private float technicalRating;
    private float theoreticalRating;
    private float codingRating;
    private float attitudeRating;
    private float teamSpiritRating;
    private String comment;
    public static FeedbackResponse_V2 buildResponse(Feedback_V2 feedback){
        //Rating_PPT rating_ppt = (Rating_PPT) feedback.getRating();
        UserDto userDto = searchUserById(feedback.getTraineeId());

        //int feedbackTypeId = feedback.getType().charAt(0) - '1';

        FeedbackResponse_V2  feedbackResponse1 = FeedbackResponse_V2.builder()
                 //.feedbackType((feedback.getType().charAt(0) - '0'))
                         .trainee(new Trainee(feedback.getTraineeId(),userDto.getName()))
                 .reviewer(new Trainee(feedback.getCreatedBy(), TrainingModuleApplication.searchNameById(feedback.getCreatedBy())))
                 .comment(feedback.getComment())
                 .build();

         if(feedback.getType().equals(VIVA_)){
             Rating_COURSE rating_course = (Rating_COURSE)  feedback.getDetails();
            feedbackResponse1.setCourse(new UserIdAndNameDto(rating_course.getCourseId(), rating_course.getCourseId()));
            feedbackResponse1.setPhase(feedback.getPhaseIds().stream().map(_id -> new UserIdAndNameDto(_id,_id)).collect(Collectors.toList()));
            feedbackResponse1.setTheoreticalRating(rating_course.getTheoreticalRating());
            feedbackResponse1.setFeedbackType(VIVA);
            feedbackResponse1.setTechnicalRating(rating_course.getTechnicalRating());
            feedbackResponse1.setCommunicationRating(rating_course.getCommunicationRating());
         }else if(feedback.getType().equals(TEST_)){
             Rating_TEST rating_test = (Rating_TEST) feedback.getDetails();
             feedbackResponse1.setTest(new UserIdAndNameDto(rating_test.getTestId(), rating_test.getTestId()));
             feedbackResponse1.setMilestone(feedback.getMilestoneIds().stream().map(_id -> new UserIdAndNameDto(_id,_id)).collect(Collectors.toList()));
             feedbackResponse1.setTheoreticalRating(rating_test.getTheoreticalRating());
             feedbackResponse1.setFeedbackType(TEST);
             feedbackResponse1.setCommunicationRating(rating_test.getCommunicationRating());
             feedbackResponse1.setCodingRating(rating_test.getCodingRating());
         }else if (feedback.getType().equals(PPT_)){
             Rating_PPT rating_ppt = (Rating_PPT) feedback.getDetails();
             feedbackResponse1.setCommunicationRating(rating_ppt.getCommunicationRating());
             feedbackResponse1.setTechnicalRating(rating_ppt.getTechnicalRating());
             feedbackResponse1.setPresentationRating(rating_ppt.getPresentationRating());
             feedbackResponse1.setFeedbackType(PPT);
             feedbackResponse1.setCourse(new UserIdAndNameDto(rating_ppt.getCourseId(), rating_ppt.getCourseId()));
         }else{
             Rating_BEHAVIOUR rating_behaviour = (Rating_BEHAVIOUR) feedback.getDetails();
             feedbackResponse1.setAttitudeRating(rating_behaviour.getAttitudeRating());
             feedbackResponse1.setFeedbackType(BEHAVIUOR);
             feedbackResponse1.setTeamSpiritRating(rating_behaviour.getTeamSpiritRating());
         }
         return feedbackResponse1;
    }
}
