package com.chicmic.trainingModule.Service.CourseServices;

import com.chicmic.trainingModule.Dto.CourseDto.CourseDto;
import com.chicmic.trainingModule.Entity.*;
import com.chicmic.trainingModule.Entity.Constants.EntityType;

import com.chicmic.trainingModule.ExceptionHandling.ApiException;
import com.chicmic.trainingModule.Repository.CourseRepo;
import com.chicmic.trainingModule.Repository.PhaseRepo;
import com.chicmic.trainingModule.Repository.SubTaskRepo;
import com.chicmic.trainingModule.Repository.TaskRepo;
import com.chicmic.trainingModule.Service.PhaseService;
import com.chicmic.trainingModule.TrainingModuleApplication;
import com.chicmic.trainingModule.Util.Pagenation;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.formula.functions.T;
import org.bson.types.ObjectId;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Collation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepo courseRepo;
    private final MongoTemplate mongoTemplate;
    private final PhaseService phaseService;


    public Course createCourse(Course course, Boolean isCourseIsAddingFromScript) {
        course.setCreatedAt(LocalDateTime.now());
        course.setUpdatedAt(LocalDateTime.now());
        course.set_id(String.valueOf(new ObjectId()));

        List<Phase<Task>> phases = phaseService.createPhases(course.getPhases(), course, EntityType.COURSE, isCourseIsAddingFromScript);
        course.setPhases(phases);

        System.out.println("course in service " + course);
        try {
            course = courseRepo.save(course);
        } catch (org.springframework.dao.DuplicateKeyException ex) {
            // Catch DuplicateKeyException and throw ApiException with 400 status
            throw new ApiException(HttpStatus.BAD_REQUEST, "Course name already exists!");
        }
        return course;
    }


    public HashMap<String, String> getCourseNamePhaseNameById(String courseId, String phaseId) {
        Query courseQuery = new Query(Criteria.where("_id").is(courseId).and("phases._id").is(phaseId));
        Course course = mongoTemplate.findOne(courseQuery, Course.class);

        HashMap<String, String> result = new HashMap<>();
//        if (course != null) {
//            result.put("courseName", course.getName());
//            System.out.println(course.getPhases().size());
//            for (Phase phase : course.getPhases()) {
//                if (phase.get_id().equals(phaseId)) {
//                    result.put("phaseName", phase.getName());
//                    break;
//                }
//            }
//        }
        return result;
    }

    public List<String> getCoursesAndTestsByTraineeId(String traineeId, Integer planType) {
        Query query1 = new Query(Criteria.where("userId").in(traineeId));
        AssignedPlan assignTask = mongoTemplate.findOne(query1, AssignedPlan.class);
        List<Plan> plans = assignTask.getPlans();

        return plans.stream()
                .flatMap(plan -> plan.getPhases().stream()
                        .flatMap(phase -> phase.getTasks().stream()
                                .filter(task -> task.getPlanType().equals(planType))
                                .map(PlanTask::getPlan)))
                .collect(Collectors.toList());
//        Aggregation aggregation = Aggregation.newAggregation(
//                Aggregation.match(Criteria.where("userId").is(traineeId)),
//                Aggregation.unwind("plans"),
//                Aggregation.lookup("plan", "plans", "_id", "courses"),
//                Aggregation.unwind("courses"),
//                Aggregation.lookup("phase", "courses.phases._id", "_id", "phases"),
//                Aggregation.unwind("phases"),
//                Aggregation.unwind("phases.tasks"),
//                Aggregation.replaceRoot("phases.tasks"),
//                Aggregation.match(Criteria.where("planType").is(1).orOperator(Criteria.where("planType").is(2)))
//        );
//
//        AggregationResults<PlanTask> results = mongoTemplate.aggregate(aggregation, AssignedPlan.class, PlanTask.class);
//        return results.getMappedResults();

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
        Collation collation = Collation.of(Locale.ENGLISH).strength(Collation.ComparisonLevel.secondary());

        Query searchQuery = new Query(finalCriteria).collation(collation).with(Sort.by(sortDirection == 1 ? Sort.Direction.ASC : Sort.Direction.DESC, sortKey));
        ;
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
//            if (sortDirection != 1) {
//                courses.sort(courseComparator.reversed());
//            } else {
//                courses.sort(courseComparator);
//            }
//        }
        List<Course> finalCourseList = new ArrayList<>();
        if (traineeId != null && !traineeId.isEmpty()) {
            List<String> courseIds = getCoursesAndTestsByTraineeId(traineeId, EntityType.COURSE);
            if (courseIds != null && courseIds.size() > 0) {
                for (Course course : courses) {
                    if (courseIds.contains(course.get_id())) {
                        finalCourseList.add(course);
                    }
                }
            }
            return finalCourseList;
        }
        return courses;
    }

    public List<Course> getAllCourses(Integer pageNumber, Integer pageSize, String query, Integer sortDirection, String sortKey, String userId) {
        if (!sortKey.isEmpty() && sortKey.equals("createdByName")) {
            Criteria criteria = Criteria.where("name").regex(query, "i")
                    .and("isDeleted").is(false);

            Criteria approvedCriteria = Criteria.where("isApproved").is(true);
            Criteria reviewersCriteria = Criteria.where("isApproved").is(false)
                    .and("approver").in(userId);
            Criteria createdByCriteria = Criteria.where("isApproved").is(false)
                    .and("createdBy").is(userId);

            // Combining the conditions
            Criteria finalCriteria = new Criteria().andOperator(
                    criteria,
                    new Criteria().orOperator(approvedCriteria, reviewersCriteria, createdByCriteria)
            );
            Query searchQuery = new Query(finalCriteria);
            List<Course> courses = mongoTemplate.find(searchQuery, Course.class);
            courses.sort(Comparator.comparing(course -> TrainingModuleApplication.searchNameById(course.getCreatedBy())));
            if (sortDirection != 1) {
                Collections.reverse(courses);
            }
            courses = Pagenation.paginateWithoutPageIndexConversion(courses, pageNumber, pageSize);
            return courses;

        }
        Pageable pageable;
        pageable = PageRequest.of(pageNumber, pageSize);

        Criteria criteria = Criteria.where("name").regex(query, "i")
                .and("isDeleted").is(false);

        Criteria approvedCriteria = Criteria.where("isApproved").is(true);
        Criteria reviewersCriteria = Criteria.where("isApproved").is(false)
                .and("approver").in(userId);
        Criteria createdByCriteria = Criteria.where("isApproved").is(false)
                .and("createdBy").is(userId);

        // Combining the conditions
        Criteria finalCriteria = new Criteria().andOperator(
                criteria,
                new Criteria().orOperator(approvedCriteria, reviewersCriteria, createdByCriteria)
        );
        Collation collation = Collation.of(Locale.ENGLISH).strength(Collation.ComparisonLevel.secondary());

        Query searchQuery = new Query(finalCriteria).with(pageable).collation(collation).with(Sort.by(sortDirection == 1 ? Sort.Direction.ASC : Sort.Direction.DESC, sortKey));

        List<Course> courses = mongoTemplate.find(searchQuery, Course.class);
//        if (!sortKey.isEmpty() && sortKey.equals("createdByName")) {
//            courses.sort(Comparator.comparing(course -> TrainingModuleApplication.searchNameById(course.getCreatedBy())));
//            if (sortDirection != 1) {
//                Collections.reverse(courses);
//            }
//        }


        return courses;
    }

    public Course getCourseById(String courseId) {
        Course course = courseRepo.findById(courseId).orElse(null);
        return course != null && course.getIsDeleted() ? null : course;
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
        try {
            Course course = courseRepo.findById(courseId).orElse(null);
            if (course != null) {
                if (courseDto.getPhases() != null) {
                    System.out.println("Course Dto ");
                    System.out.println(courseDto.getPhases());
                    List<Phase<Task>> phases = phaseService.createPhases(courseDto.getPhases(), course, EntityType.COURSE, false);
                    course.setPhases(phases);
                }
                if (courseDto.getName() != null) {
                    course.setName(courseDto.getName());
                }
                if (courseDto.getFigmaLink() != null) {
                    course.setFigmaLink(courseDto.getFigmaLink());
                }
                if (courseDto.getGuidelines() != null) {
                    course.setGuidelines(courseDto.getGuidelines());
                }
                if (courseDto.getApprover() != null) {
                    System.out.println("Insdie update " + courseDto);
                    course.setApprover(courseDto.getApprover());
                    Integer count = 0;
                    for (String reviewer : course.getApprover()) {
                        if (course.getApprovedBy().contains(reviewer)) {
                            count++;
                        }
                    }
                    if (count == course.getApprover().size()) {
                        course.setIsApproved(true);
                    } else {
                        course.setIsApproved(false);
                    }

                    Set<String> approvedBy = new HashSet<>();
                    for (String approver : course.getApprovedBy()) {
                        if (course.getApprover().contains(approver)) {
                            approvedBy.add(approver);
                        }
                    }
                    course.setApprovedBy(approvedBy);
                }
                try {
                    course = courseRepo.save(course);
                } catch (org.springframework.dao.DuplicateKeyException ex) {
                    // Catch DuplicateKeyException and throw ApiException with 400 status
                    throw new ApiException(HttpStatus.BAD_REQUEST, "Course name already exists!");
                }
                return course;
            } else {
                return null;
            }
        } catch (Exception e) {
            // The exception will automatically trigger a rollback
            throw new ApiException(HttpStatus.BAD_REQUEST, e.getMessage());

        }
    }

    public long countNonDeletedCourses(String query, String userId) {

        Criteria criteria = Criteria.where("name").regex(query, "i")
                .and("isDeleted").is(false);

        Criteria approvedCriteria = Criteria.where("isApproved").is(true);
        Criteria reviewersCriteria = Criteria.where("isApproved").is(false)
                .and("approver").in(userId);
        Criteria createdByCriteria = Criteria.where("isApproved").is(false)
                .and("createdBy").is(userId);

        // Combining the conditions
        Criteria finalCriteria = new Criteria().andOperator(
                criteria,
                new Criteria().orOperator(approvedCriteria, reviewersCriteria, createdByCriteria)
        );
        MatchOperation matchStage = Aggregation.match(finalCriteria);

        Aggregation aggregation = Aggregation.newAggregation(matchStage);
        AggregationResults<Course> aggregationResults = mongoTemplate.aggregate(aggregation, "course", Course.class);
        return aggregationResults.getMappedResults().size();
    }

    public Course approve(Course course, String userId) {
        Set<String> approvedBy = course.getApprovedBy();
        approvedBy.add(userId);
        course.setApprovedBy(approvedBy);
        if (course.getApprover().size() == approvedBy.size()) {
            course.setIsApproved(true);
        } else {
            course.setIsApproved(false);
        }
        return courseRepo.save(course);
    }

    public List<Map<String, String>> findCoursesByIds(List<String> Ids) {
        Criteria criteria = Criteria.where("_id").in(Ids);
        Query query = new Query(criteria);
        query.fields().include("_id", "name", "phases._id", "phases.name");
        List<Course> course = mongoTemplate.find(new Query(criteria), Course.class);
//        HashMap<String,String> courseDetails = new HashMap<>();
        List<Map<String, String>> courseDetailsList = Arrays.asList(new HashMap<>(), new HashMap<>());
        course.forEach(c -> {
            courseDetailsList.get(0).put(c.get_id(), c.getName());
            c.getPhases().forEach(p -> {
                courseDetailsList.get(1).put(p.get_id(), p.getName());
            });
        });
        return courseDetailsList;
    }
}
