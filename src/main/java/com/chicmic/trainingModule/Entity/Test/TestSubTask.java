package com.chicmic.trainingModule.Entity.Test;

import com.chicmic.trainingModule.Entity.SubTask;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestSubTask extends SubTask {
    private String reference;
}
