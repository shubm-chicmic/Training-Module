package com.chicmic.trainingModule.Dto.DashboardDto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Builder
public class RatingDto {
    private Float totalRating;
    private Integer count;

    public RatingDto(Float totalRating, Integer count) {
        this.totalRating = totalRating;
        this.count = count;
    }
    public void incrTotalRating(float val){
        totalRating += val;
    }
    public void incrcount(){
        count += 1;
    }
}
