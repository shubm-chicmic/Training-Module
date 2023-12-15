package com.chicmic.trainingModule.Entity.Course;
import com.chicmic.trainingModule.Entity.SubTask;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseSubTask extends SubTask {
   private String link;
}
