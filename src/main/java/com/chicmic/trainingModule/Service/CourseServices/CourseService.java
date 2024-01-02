package com.chicmic.trainingModule.Service.CourseServices;

import com.chicmic.trainingModule.Dto.CourseDto.CourseDto;
import com.chicmic.trainingModule.Entity.*;
import com.chicmic.trainingModule.Entity.Constants.EntityType;

import com.chicmic.trainingModule.Repository.CourseRepo;
import com.chicmic.trainingModule.Repository.PhaseRepo;
import com.chicmic.trainingModule.Repository.SubTaskRepo;
import com.chicmic.trainingModule.Repository.TaskRepo;
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
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepo courseRepo;
    private final MongoTemplate mongoTemplate;
    private final PhaseRepo phaseRepo;
    private final TaskRepo taskRepo;
    private final SubTaskRepo subTaskRepo;

    public Course createCourse(Course course) {
        course.setCreatedAt(LocalDateTime.now());
        course.setUpdatedAt(LocalDateTime.now());
        List<Phase<Task>> phases = new ArrayList<>();
        int count = 0;
        course.set_id(String.valueOf(new ObjectId()));
        for (Phase<Task> phase : course.getPhases()) {
            phase.set_id(String.valueOf(new ObjectId()));
            count++;
            List<Task> tasks = new ArrayList<>();
            for (Task task : phase.getTasks()) {
                task.set_id(String.valueOf(new ObjectId()));
                List<SubTask> subTasks = new ArrayList<>();
                for (SubTask subTask : task.getSubtasks()) {
                    subTask.setEntityType(EntityType.COURSE);
                    subTask.setTask(task);
                    subTasks.add(subTaskRepo.save(subTask));
                }
                task.setEntityType(EntityType.COURSE);
                task.setSubtasks(subTasks);
                task.setPhase(phase);
                tasks.add(taskRepo.save(task));
            }
            phase.setName("Phase " + count);
            phase.setEntityType(EntityType.COURSE);
            phase.setTasks(tasks);
            phase.setEntity(course);
            phases.add(phaseRepo.save(phase));
        }
        course.setPhases(phases);
        course = courseRepo.save(course);
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
            AssignedPlan assignTask = mongoTemplate.findOne(query1, AssignedPlan.class);
//            AssignTask assignTask = AssignTaskService.getAllAssignTasksByTraineeId(traineeId);
//            if(assignTask != null) {
//                for (Plan plan : assignTask.getPlans()) {
//                    for (com.chicmic.trainingModule.Entity.Plan33.Phase phase : plan.getPhases()) {
//                        for (PlanTask planTask : phase.getTasks()) {
//                            if (planTask.getPlanType() == 1) {
//                                for (Course course : courses) {
//                                    if (course.get_id().equals(((AssignTaskPlanTrack) planTask.getPlan()).get_id())) {
//                                        finalCourseList.add(course);
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
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
                .and("approver").in(userId);
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
    public Phase<Task> getPhaseById(String phaseId) {
        return phaseRepo.findById(phaseId).orElse(null);
    }
    public List<Phase> getPhaseByIds(List<String> phaseId) {
        return phaseRepo.findAllById(phaseId);
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
            if (courseDto.getPhases() != null) {
//                List<Phase> phases = new ArrayList<>();
//                for (List<Task> courseTasks : courseDto.getPhases()) {
//                    Phase phase = Phase.builder()
//                            .entityType(EntityType.COURSE)
//                            .tasks(courseTasks)
//                            .build();
//                    phases.add(phase);
//                }
                List<Phase<Task>> phases = new ArrayList<>();
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
                    Phase<Task> phase = Phase.<Task>builder()
                            ._id(String.valueOf(new ObjectId()))
                            .tasks(courseDto.getPhases().get(j))
                            .build();
                    phases.add(phase);
                    j++;
                }
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
                course.setApprover(courseDto.getApprover());
                Integer count = 0;
                for (String reviewer : course.getApprover()){
                    if(course.getApprovedBy().contains(reviewer)){
                        count++;
                    }
                }
                if(count == course.getApprover().size()){
                    course.setIsApproved(true);
                }else {
                    course.setIsApproved(false);
                }

                Set<String> approvedBy = new HashSet<>();
                for (String approver : course.getApprovedBy()){
                    if(course.getApprover().contains(approver)){
                       approvedBy.add(approver);
                    }
                }
                course.setApprovedBy(approvedBy);
            }
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
        if(course.getApprover().size() == approvedBy.size()) {
            course.setIsApproved(true);
        }else {
            course.setIsApproved(false);
        }
        return course;
    }
    public List<Map<String,String>> findCoursesByIds(List<String> Ids){
        Criteria criteria = Criteria.where("_id").in(Ids);
        Query query = new Query(criteria);
        query.fields().include("_id","name","phases._id","phases.name");
        List<Course> course = mongoTemplate.find(new Query(criteria),Course.class);
//        HashMap<String,String> courseDetails = new HashMap<>();
        List<Map<String,String>> courseDetailsList = Arrays.asList(new HashMap<>(),new HashMap<>());
        course.forEach(c -> {
            courseDetailsList.get(0).put(c.get_id(),c.getName());
            c.getPhases().forEach(p -> {
                courseDetailsList.get(1).put(p.get_id(),p.getName());
            });
        });
        return courseDetailsList;
    }
}
