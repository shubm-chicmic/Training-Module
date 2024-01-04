package com.chicmic.trainingModule.Entity;

import com.chicmic.trainingModule.Dto.FeedbackDto.FeedbackRequestDto;
import com.chicmic.trainingModule.Dto.rating.Rating;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import static com.chicmic.trainingModule.Dto.rating.Rating.getRating;
import static com.chicmic.trainingModule.Dto.rating.Rating.getSubTaskIds;
import static com.chicmic.trainingModule.Util.FeedbackUtil.FEEDBACK_TYPE_CATEGORY_V2;

@Getter @Setter @Builder
@Document
public class Feedback_V2 {
    @Id
    private String _id;
    private String traineeId;
    private String type;
    private Float overallRating;
    private Rating details;
    private Set<String> phaseIds;//phaseids,milestoneids,courseids
    private Set<String> milestoneIds;
    private String comment;
    private String createdAt;
    private String updateAt;
    private String createdBy;
    private boolean isDeleted;

    public static Feedback_V2 buildFeedbackFromFeedbackRequestDto(FeedbackRequestDto feedbackDto,String reviewer){
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
       // String type = FEEDBACK_TYPE_CATEGORY_V2[feedbackDto.getFeedbackType().charAt(0) - '1'];

        Feedback_V2 feedbackV2 =  Feedback_V2.builder()
                .traineeId(feedbackDto.getTrainee())
                .type(feedbackDto.getFeedbackType())
                .details(getRating(feedbackDto))
                .comment(feedbackDto.getComment())
                .createdAt(formatter.format(date))
                .updateAt(formatter.format(date))
                .createdBy(reviewer)
                .overallRating(feedbackDto.computeRating())
                .isDeleted(false)
                .build();
        if (feedbackDto.getFeedbackType().equals("1"))
            feedbackV2.setPhaseIds(feedbackDto.getPhase());
        else if(feedbackDto.getFeedbackType().equals("2"))
            feedbackV2.setMilestoneIds(feedbackDto.getMilestone());

        return feedbackV2;
    }
}
