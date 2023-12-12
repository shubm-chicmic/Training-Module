package com.chicmic.trainingModule.Service.CourseServices;

import com.chicmic.trainingModule.Dto.CourseDto.CourseDto;
import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.chicmic.trainingModule.Entity.MomMessage;
import com.chicmic.trainingModule.Entity.Phase;
import com.chicmic.trainingModule.Entity.StatusConstants;
import com.chicmic.trainingModule.Repository.CourseRepo;
import com.chicmic.trainingModule.Util.CustomObjectMapper;
import com.chicmic.trainingModule.Entity.Course;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepo courseRepo;
    private final MongoTemplate mongoTemplate;

    public Course createCourse(Course course) {
        course = courseRepo.save(course);
        return course;
    }

    public List<Course> getAllCourses(Integer pageNumber, Integer pageSize, String query, Integer sortDirection, String sortKey) {
        Pageable pageable;
        if (!sortKey.isEmpty()) {
            Sort.Direction direction = (sortDirection == 0) ? Sort.Direction.ASC : Sort.Direction.DESC;
            Sort sort = Sort.by(direction, sortKey);
            pageable = PageRequest.of(pageNumber, pageSize, sort);
        } else {
            pageable = PageRequest.of(pageNumber, pageSize);
        }

        Query searchQuery = new Query()
                .addCriteria(Criteria.where("name").regex(query, "i"))
                .addCriteria(Criteria.where("isDeleted").is(false))
                .with(pageable);

        List<Course> courses = mongoTemplate.find(searchQuery, Course.class);

        if (!sortKey.isEmpty()) {
            Comparator<Course> courseComparator = Comparator.comparing(course -> {
                try {
                    Field field = Course.class.getDeclaredField(sortKey);
                    field.setAccessible(true);
                    Object value = field.get(course);
                    if (value instanceof String) {
                        return ((String) value).toLowerCase();
                    }
                    return value.toString();
                } catch (Exception e) {
                    e.printStackTrace();
                    return "";
                }
            });

            if (sortDirection == 1) {
                courses.sort(courseComparator.reversed());
            } else {
                courses.sort(courseComparator);
            }
        }

        return courses;
    }

    public Course getCourseById(String courseId) {
        return courseRepo.findById(courseId).orElse(null);
    }

    public Boolean deleteCourseById(String courseId) {
        Course course = courseRepo.findById(courseId).orElse(null);
        if (course != null) {
            course.setIsDeleted(true);
            courseRepo.save(course);
            return true;
        } else {
            return false;
        }
    }

    public Course updateStatus(String courseId, int status) {
        Course course = courseRepo.findById(courseId).orElse(null);
        if (course != null) {
            course.setStatus(status);
            courseRepo.save(course);
            return course;
        } else {
            return null;
        }
    }

    public Course updateCourse(CourseDto courseDto, String courseId) {
        Course course = courseRepo.findById(courseId).orElse(null);
        if (course != null) {
            List<List<Phase>> nestedPhases = courseDto.getPhases();
            List<Phase> flatPhases = nestedPhases.stream()
                    .flatMap(List::stream)
                    .toList();
            Set<String> reviewerIds = courseDto.getReviewers().stream()
                    .map(UserIdAndNameDto::get_id)
                    .collect(Collectors.toSet());
            course =  Course.builder()
                    ._id(courseId)
                    .name(courseDto.getName())
                    .figmaLink(courseDto.getFigmaLink())
                    .guidelines(courseDto.getGuidelines())
                    .reviewers(reviewerIds)
                    .phases(flatPhases)
                    .isApproved(course.getIsApproved())
                    .isDeleted(course.getIsDeleted())
                    .createdBy(course.getCreatedBy())
                    .build();
            courseRepo.save(course);
            return course;
        } else {
            return null;
        }
    }

    public long countNonDeletedCourses() {
        MatchOperation matchStage = Aggregation.match(Criteria.where("isDeleted").is(false));
        Aggregation aggregation = Aggregation.newAggregation(matchStage);
        AggregationResults<Course> aggregationResults = mongoTemplate.aggregate(aggregation, "course", Course.class);
        return aggregationResults.getMappedResults().size();
    }
    public Course approve(Course course, String userId) {
        Set<String> approvedBy = course.getApprovedBy();
        approvedBy.add(userId);
        course.setApprovedBy(approvedBy);
        if(course.getReviewers().size() == approvedBy.size()) {
            course.setIsApproved(true);
            course.setStatus(StatusConstants.UPCOMING);
        }else {
            course.setIsApproved(false);
        }
        return courseRepo.save(course);
    }
}
