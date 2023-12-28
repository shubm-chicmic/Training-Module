package com.chicmic.trainingModule.Controller;

import com.chicmic.trainingModule.Entity.Course;
import com.chicmic.trainingModule.ExcelPerformOperations;
import com.chicmic.trainingModule.Service.CourseServices.CourseService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/addCourseWithScript")
@AllArgsConstructor
public class ScriptController {
    private final CourseService courseService;
    @GetMapping
    public void findExcelFilesAndCreateCourse() {
        try {
            Path rootDir = Paths.get("/home/chicmic/IdeaProjects/trainingModule/TrainingModuleExcelSearch"); // Change this to your project root directory

            Files.walk(rootDir)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase().endsWith(".xlsx"))
                    .forEach(path -> {
                        System.out.println("Processing file: " + path);
                        Course course = ExcelPerformOperations.excelPerformOperations(path.toString());
                        System.out.println(course.getName());
                        courseService.createCourse(course);
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
