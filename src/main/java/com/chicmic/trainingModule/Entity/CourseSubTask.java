package com.chicmic.trainingModule.Entity;
import lombok.*;
import org.bson.types.ObjectId;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseSubTask extends SubTask{
   private String link;
}
