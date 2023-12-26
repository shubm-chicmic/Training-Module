package com.chicmic.trainingModule.Entity.Test;

import com.chicmic.trainingModule.Entity.Test.TestTask;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class Milestone {
    public static int count = 0;
    @Id
    private String _id;
    @Transient
    private String name;
    @Transient
    private String estimatedTime;
    @Transient
    private Integer noOfTasks;
    private List<TestTask> tasks;
    public Milestone() {
        count++;
        this.name = "Milestone " + count;
    }

    public String getEstimatedTime() {
        long phaseHours = 0;
        long phaseMinutes = 0;
        long totalHours = 0;
        long totalMinutes = 0;
        for(TestTask task : tasks) {
            for (TestSubTask testSubTask : task.getSubtasks()) {
                String[] timeParts = testSubTask.getEstimatedTime().split(":");
                if (timeParts.length == 1) {
                    phaseHours += (timeParts[0] != null && !timeParts[0].isEmpty()) ? Long.parseLong(timeParts[0]) : 0;
                } else if (timeParts.length == 2) {
                    phaseHours += (timeParts[0] != null && !timeParts[0].isEmpty()) ? Long.parseLong(timeParts[0]) : 0;
                    phaseMinutes += (timeParts[1] != null && !timeParts[1].isEmpty()) ? Long.parseLong(timeParts[1]) : 0;
                }
            }
        }
        totalHours += phaseHours + phaseMinutes / 60;
        totalMinutes += phaseMinutes % 60;
        return estimatedTime = String.format("%02d:%02d", totalHours, totalMinutes);
    }
}
