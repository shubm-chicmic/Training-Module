package com.chicmic.trainingModule.Dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Builder
public class TraineeRating {
    int index;
    double rating;
    int count;

    public TraineeRating(int index, double rating, int count) {
        this.index = index;
        this.rating = rating;
        this.count = count;
    }
    public void incrRating(double val){
        rating += val;
    }
    public void incrCount(){
        count += 1;
    }
}
