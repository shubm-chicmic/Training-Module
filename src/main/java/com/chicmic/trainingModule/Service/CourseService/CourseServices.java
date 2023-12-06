package com.chicmic.trainingModule.Service.CourseService;

import com.chicmic.trainingModule.Dto.CourseDto;
import com.chicmic.trainingModule.Entity.Course;
import com.chicmic.trainingModule.Repository.CourseRepo;
import com.chicmic.trainingModule.Util.CustomObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class CourseServices {
    private final CourseRepo courseRepo;
//    @Bean
//    private void clearCourse(){
//        courseRepo.deleteAll();
//    }

    public Course createCourse(Course course) {
        course = courseRepo.save(course);
        return course;
    }

    public List<Course> getAllCourses(Integer pageNumber, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        List<Course> courses = courseRepo.findAll(pageable).getContent();
        return courses;
    }

    public Course getCourseById(String courseId) {
        return courseRepo.findById(courseId).orElse(null);
    }

    public Boolean deleteCourseById(String courseId) {
        Course course = courseRepo.findById(courseId).orElse(null);
        if (course != null) {
            course.setIsDeleted(false);
            courseRepo.save(course);
            return true;
        } else {
            return false;
        }
    }
    public Course updateCourse(CourseDto courseDto, String courseId) {
        Course course = courseRepo.findById(courseId).orElse(null);
        if (course != null) {
            course = (Course) CustomObjectMapper.updateFields(courseDto, course);
            courseRepo.save(course);
            return course;
        } else {
            return null;
        }
    }


}
