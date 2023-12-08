package com.chicmic.trainingModule.Entity;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubTask {
    private String taskName;
    private String time;
    private String url;
}
