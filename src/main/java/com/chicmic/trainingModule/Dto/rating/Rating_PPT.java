package com.chicmic.trainingModule.Dto.rating;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class Rating_PPT implements Rating{
    private Float communicationRating;
    private Float technicalRating;
    private Float presentationRating;
    private String courseId;
    public Float computeOverallRating(){
        float total = communicationRating + technicalRating + presentationRating;
        return total/3;
    }
}
