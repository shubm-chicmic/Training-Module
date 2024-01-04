package com.chicmic.trainingModule.Dto.FeedbackDto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Builder
public class RatingAndCountDto {
    private int count;
    private Float rating;

    public RatingAndCountDto() {
    }

    public RatingAndCountDto(int count, Float rating) {
        this.count = count;
        this.rating = rating;
    }
}
