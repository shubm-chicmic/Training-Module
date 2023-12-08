package com.chicmic.trainingModule.Entity;

import lombok.*;

import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Phase {
    private String name;
    private List<Task> tasks;
}
