//package com.chicmic.trainingModule.Dto.FeedbackResponseDto;
//
//import com.chicmic.trainingModule.Dto.UserDto;
//import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
//import com.chicmic.trainingModule.Dto.ratings.Rating_COURSE;
//import com.chicmic.trainingModule.Entity.Feedback;
//import lombok.Builder;
//import lombok.Getter;
//import lombok.Setter;
//
//import java.util.Date;
//
//import static com.chicmic.trainingModule.TrainingModuleApplication.searchUserById;
//import static com.chicmic.trainingModule.Util.FeedbackUtil.FEEDBACK_TYPE_CATEGORY;
//
//@Getter @Setter @Builder
//public class FeedbackResponse_COURSE implements FeedbackResponse{
//    private String _id;
//    private UserDto reviewer;
//    private UserDto trainee;
//    private UserIdAndNameDto feedbackType;
//    private UserIdAndNameDto task;
//    private UserIdAndNameDto subTask;
//    private Float theoreticalRating;
//    private Float technicalRating;
//    private Float communicationRating;
//    private Date createdOn;
//    private Float rating;
//    private String comment;
//    private Float overallRating;
//
//    public static FeedbackResponse buildFeedbackResponse(Feedback feedback){
//        Rating_COURSE rating_course = (Rating_COURSE) feedback.getRating();
//        UserDto trainee = searchUserById(feedback.getTraineeID());
//        UserDto reviewer = searchUserById(feedback.getCreatedBy());
//        int feedbackTypeId = feedback.getType().charAt(0) - '1';
//
//        return FeedbackResponse_COURSE.builder()
//                ._id(feedback.getId())
//                .reviewer(reviewer)
//                .trainee(trainee)
//                .comment(feedback.getComment())
//                .theoreticalRating(rating_course.getTheoreticalRating())
//                .technicalRating(rating_course.getTechnicalRating())
//                .communicationRating(rating_course.getCommunicationRating())
//                .feedbackType(new UserIdAndNameDto("1",FEEDBACK_TYPE_CATEGORY[feedbackTypeId]))
//                .task(new UserIdAndNameDto(rating_course.getCourseId(), rating_course.getCourseId()))
//                .subTask(new UserIdAndNameDto(rating_course.getPhaseId(),rating_course.getPhaseId()))
//                .createdOn(feedback.getCreatedAt())
//                .rating(feedback.getOverallRating())
//                .build();
//    }
//
//}
