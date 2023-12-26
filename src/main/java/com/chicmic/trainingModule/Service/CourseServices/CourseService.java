package com.chicmic.trainingModule.Service.CourseServices;

import com.chicmic.trainingModule.Dto.CourseDto.CourseDto;
import com.chicmic.trainingModule.Entity.AssignTask.AssignTask;
import com.chicmic.trainingModule.Entity.AssignTask.AssignTaskPlanTrack;
import com.chicmic.trainingModule.Entity.Course.Course;
import com.chicmic.trainingModule.Entity.Course.CourseTask;
import com.chicmic.trainingModule.Entity.Course.Phase;
import com.chicmic.trainingModule.Entity.Plan.Plan;
import com.chicmic.trainingModule.Entity.Plan.Task;
import com.chicmic.trainingModule.Repository.CourseRepo;
import com.chicmic.trainingModule.Service.AssignTaskService.AssignTaskService;
import com.mongodb.BasicDBObject;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepo courseRepo;
    private final MongoTemplate mongoTemplate;

    public Course createCourse(Course course) {
        course.setCreatedAt(LocalDateTime.now());
        course.setUpdatedAt(LocalDateTime.now());
        course = courseRepo.save(course);
        return course;
    }
    public HashMap<String, String> getCourseNamePhaseNameById(String courseId, String phaseId) {
        Query courseQuery = new Query(Criteria.where("_id").is(courseId).and("phases._id").is(phaseId));
        Course course = mongoTemplate.findOne(courseQuery, Course.class);

        HashMap<String, String> result = new HashMap<>();
        if (course != null) {
            result.put("courseName", course.getName());
            System.out.println(course.getPhases().size());
            for (Phase phase : course.getPhases()) {
                if (phase.get_id().equals(phaseId)) {
                    result.put("phaseName", phase.getName());
                    break;
                }
            }
        }
        return result;
    }

    public List<Course> getAllCourses(String query, Integer sortDirection, String sortKey, String traineeId) {
        Criteria criteria = Criteria.where("name").regex(query, "i")
                .and("isDeleted").is(false);

        Criteria approvedCriteria = Criteria.where("isApproved").is(true);


        // Combining the conditions
        Criteria finalCriteria = new Criteria().andOperator(
                criteria,
                new Criteria().orOperator(approvedCriteria)
        );

        Query searchQuery = new Query(finalCriteria);

        List<Course> courses = mongoTemplate.find(searchQuery, Course.class);

//        if (!sortKey.isEmpty()) {
//            Comparator<Course> courseComparator = Comparator.comparing(course -> {
//                try {
//                    Field field = Course.class.getDeclaredField(sortKey);
//                    field.setAccessible(true);
//                    Object value = field.get(course);
//                    if (value instanceof String) {
//                        return ((String) value).toLowerCase();
//                    }
//                    return value.toString();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    return "";
//                }
//            });
//
//            if (sortDirection == 1) {
//                courses.sort(courseComparator.reversed());
//            } else {
//                courses.sort(courseComparator);
//            }
//        }
        List<Course> finalCourseList = new ArrayList<>();
        if(traineeId != null && !traineeId.isEmpty()) {
            Query query1 = new Query(Criteria.where("userId").in(traineeId));
            AssignTask assignTask = mongoTemplate.findOne(query1, AssignTask.class);
//            AssignTask assignTask = AssignTaskService.getAllAssignTasksByTraineeId(traineeId);
            if(assignTask != null) {
                for (Plan plan : assignTask.getPlans()) {
                    for (com.chicmic.trainingModule.Entity.Plan.Phase phase : plan.getPhases()) {
                        for (Task task : phase.getTasks()) {
                            if (task.getPlanType() == 1) {
                                for (Course course : courses) {
                                    if (course.get_id().equals(((AssignTaskPlanTrack) task.getPlan()).get_id())) {
                                        finalCourseList.add(course);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return finalCourseList;
        }
        return courses;
    }

    public List<Course> getAllCourses(Integer pageNumber, Integer pageSize, String query, Integer sortDirection, String sortKey, String userId) {
        Pageable pageable;
        if (!sortKey.isEmpty()) {
            Sort.Direction direction = (sortDirection == 0) ? Sort.Direction.ASC : Sort.Direction.DESC;
            Sort sort = Sort.by(direction, sortKey);
            pageable = PageRequest.of(pageNumber, pageSize, sort);
        } else {
            pageable = PageRequest.of(pageNumber, pageSize);
        }


        Criteria criteria = Criteria.where("name").regex(query, "i")
                .and("isDeleted").is(false);

        Criteria approvedCriteria = Criteria.where("isApproved").is(true);
        Criteria reviewersCriteria = Criteria.where("isApproved").is(false)
                .and("reviewers").in(userId);
        Criteria createdByCriteria = Criteria.where("isApproved").is(false)
                .and("createdBy").is(userId);

        // Combining the conditions
        Criteria finalCriteria = new Criteria().andOperator(
                criteria,
                new Criteria().orOperator(approvedCriteria, reviewersCriteria, createdByCriteria)
        );

        Query searchQuery = new Query(finalCriteria).with(pageable);

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

    public Course updateCourse(CourseDto courseDto, String courseId) {
        Course course = courseRepo.findById(courseId).orElse(null);
        if (course != null) {
//            List<Phase> phases = new ArrayList<>();
            if (courseDto.getPhases() != null) {
                List<Phase> phases = new ArrayList<>();
                int i = 0, j = 0;
                System.out.println("Course Phase size = " + course.getPhases().size());
                System.out.println("CourseDto Phase size = " + courseDto.getPhases().size());

                while(i < course.getPhases().size() && j < courseDto.getPhases().size()){
                    Phase phase = course.getPhases().get(i);
                    phase.setTasks(courseDto.getPhases().get(j));
                    i++;
                    j++;
                    phases.add(phase);
                }
//                while(i < course.getPhases().size()){
//                    phases.add(course.getPhases().get(i));
//                    i++;
//                }
                while(j < courseDto.getPhases().size()){
                    Phase phase = Phase.builder()
                            ._id(String.valueOf(new ObjectId()))
                            .tasks(courseDto.getPhases().get(j))
                            .build();
                    phases.add(phase);
                    j++;
                }
                course.setPhases(phases);
//                for (int i = 0; i < courseDto.getPhases().size(); i++) {
//                    Phase phase = course.getPhases().get(i);
//                    phase.setTasks(courseDto.getPhases().get(i));
//                }
//                for (List<CourseTask> courseTasks : courseDto.getPhases()) {
//                    for (Phase phase : course.getPhases()) {
//                        phase.setTasks(courseTasks);
//                    }
//                    }
//                    Phase phase = Phase.builder()
//                            .tasks(courseTasks)
//                            .build();
//                    phases.add(phase);

            }
            // Only update properties from the DTO if they are not null
            if (courseDto.getName() != null) {
                course.setName(courseDto.getName());
            }
            if (courseDto.getFigmaLink() != null) {
                course.setFigmaLink(courseDto.getFigmaLink());
            }
            if (courseDto.getGuidelines() != null) {
                course.setGuidelines(courseDto.getGuidelines());
            }
            if (courseDto.getReviewers() != null) {
                course.setReviewers(courseDto.getReviewers());
                Integer count = 0;
                for (String reviewer : course.getReviewers()){
                    if(course.getApprovedBy().contains(reviewer)){
                        count++;
                    }
                }
                if(count == course.getReviewers().size()){
                    course.setIsApproved(true);
                }else {
                    course.setIsApproved(false);
                }

                Set<String> approvedBy = new HashSet<>();
                for (String approver : course.getApprovedBy()){
                    if(course.getReviewers().contains(approver)){
                       approvedBy.add(approver);
                    }
                }
                course.setApprovedBy(approvedBy);
            }
//            if (!phases.isEmpty()) {
//                course.setPhases(phases);
//            }
            // Saving the updated course
            courseRepo.save(course);
            return course;
        }else {
            return null;
        }
    }

    public long countNonDeletedCourses(String query) {
        MatchOperation matchStage = Aggregation.match(Criteria.where("name").regex(query, "i")
                .and("isDeleted").is(false));

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
        }else {
            course.setIsApproved(false);
        }
        return courseRepo.save(course);
    }

    public List<Phase> getCourseByPhaseIds(String courseId, List<Object> phaseIds) {
        System.out.println("course id = " + courseId + " " + phaseIds );
        Course course = mongoTemplate.findById(courseId, Course.class); // Retrieve course by ID
        System.out.println("course Id = " + course.get_id());
        System.out.println(course);
        for (Phase phase : course.getPhases()) {
            System.out.println("phaseId = " + phase.get_id());
        }
        if (course != null) {
            List<Phase> phases = new ArrayList<>();

            for (Object phaseId : phaseIds) {
                String strPhaseId = phaseId.toString();

                // Find phase by ID and add it to the list if found in the course's phases
                Phase foundPhase = course.getPhases().stream()
                        .filter(phase -> strPhaseId.equals(phase.get_id()))
                        .findFirst()
                        .orElse(null);

                if (foundPhase != null) {
                    phases.add(foundPhase);
                }
            }

            return phases;
        } else {
            return Collections.emptyList();
        }
    }

}
