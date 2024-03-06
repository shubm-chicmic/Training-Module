package com.chicmic.trainingModule.Dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class Test {
    private String _id;
    private String testName;

    public Test(String _id, String testName) {
        this._id = _id;
        this.testName = testName;
    }
}
