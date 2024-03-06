package com.chicmic.trainingModule.Dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Course {
    private String _id;
    private String courseName;

    public Course(String _id, String courseName) {
        this._id = _id;
        this.courseName = courseName;
    }
}
