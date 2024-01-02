package com.chicmic.trainingModule.Dto.rating;

import com.chicmic.trainingModule.Util.FeedbackUtil;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
@Builder
@Getter
@Setter
public class Rating_COURSE implements Rating{
    private String taskId;
    private Float theoreticalRating;
    private Float technicalRating;
    private Float communicationRating;
    public Float computeOverallRating(){
        float total = theoreticalRating + technicalRating + communicationRating;
        return total/3;
    }
}
