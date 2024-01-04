package com.chicmic.trainingModule.Service.TestServices;

import com.chicmic.trainingModule.Dto.TestDto.TestDto;
import com.chicmic.trainingModule.Entity.*;
import com.chicmic.trainingModule.Entity.Constants.EntityType;
import com.chicmic.trainingModule.Repository.*;
import lombok.RequiredArgsConstructor;
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
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;

@Service
@RequiredArgsConstructor
public class TestService {
    private final TestRepo testRepo;
    private final MongoTemplate mongoTemplate;
    private final PhaseRepo phaseRepo;
    private final TaskRepo taskRepo;
    private final SubTaskRepo subTaskRepo;

    public Test createTest(Test test) {
        test.setCreatedAt(LocalDateTime.now());
        test.setUpdatedAt(LocalDateTime.now());
        List<Phase<Task>> milestones = new ArrayList<>();
        int count = 0;
        test.set_id(String.valueOf(new ObjectId()));
        for (Phase<Task> milestone : test.getMilestones()) {
            milestone.set_id(String.valueOf(new ObjectId()));
            count++;
            List<Task> tasks = new ArrayList<>();
            for (Task task : milestone.getTasks()) {
                task.set_id(String.valueOf(new ObjectId()));
                List<SubTask> subTasks = new ArrayList<>();
                for (SubTask subTask : task.getSubtasks()) {
                    subTask.setEntityType(EntityType.TEST);
                    subTask.setTask(task);
                    subTasks.add(subTaskRepo.save(subTask));
                }
                task.setEntityType(EntityType.TEST);
                task.setSubtasks(subTasks);
                task.setPhase(milestone);
                tasks.add(taskRepo.save(task));
            }
            milestone.setName("Milestone " + count);
            milestone.setEntityType(EntityType.TEST);
            milestone.setTasks(tasks);
            milestone.setEntity(test);
            milestones.add(phaseRepo.save(milestone));
        }
        test.setMilestones(milestones);
        test = testRepo.save(test);
        return test;
    }

    public List<Test> getAllTests(String query, Integer sortDirection, String sortKey) {
        Criteria criteria = Criteria.where("testName").regex(query, "i")
                .and("deleted").is(false);

        Criteria approvedCriteria = Criteria.where("approved").is(true);


        // Combining the conditions
        Criteria finalCriteria = new Criteria().andOperator(
                criteria,
                new Criteria().orOperator(approvedCriteria)
        );

        Query searchQuery = new Query(finalCriteria);

        List<Test> tests = mongoTemplate.find(searchQuery, Test.class);

        if (!sortKey.isEmpty()) {
            Comparator<Test> testComparator = Comparator.comparing(test -> {
                try {
                    Field field = Test.class.getDeclaredField(sortKey);
                    field.setAccessible(true);
                    Object value = field.get(test);
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
                tests.sort(testComparator.reversed());
            } else {
                tests.sort(testComparator);
            }
        }

        return tests;
    }
    public List<Test> getAllTests(Integer pageNumber, Integer pageSize, String query, Integer sortDirection, String sortKey, String userId) {
        Pageable pageable;
        if (!sortKey.isEmpty()) {
            Sort.Direction direction = (sortDirection == 0) ? Sort.Direction.ASC : Sort.Direction.DESC;
            Sort sort = Sort.by(direction, sortKey);
            pageable = PageRequest.of(pageNumber, pageSize, sort);
        } else {
            pageable = PageRequest.of(pageNumber, pageSize);
        }
        Criteria criteria = Criteria.where("testName").regex(query, "i")
                .and("deleted").is(false);

        Criteria approvedCriteria = Criteria.where("approved").is(true);
        Criteria reviewersCriteria = Criteria.where("approved").is(false)
                .and("approver").in(userId);
        Criteria createdByCriteria = Criteria.where("approved").is(false)
                .and("createdBy").is(userId);

        Criteria finalCriteria = new Criteria().andOperator(
                criteria,
                new Criteria().orOperator(approvedCriteria, reviewersCriteria, createdByCriteria)
        );
        Query searchQuery = new Query(finalCriteria).with(pageable);
//        Query searchQuery = new Query()
//                .addCriteria(Criteria.where("testName").regex(query, "i"))
//                .addCriteria(Criteria.where("deleted").is(false))
//                .with(pageable);

        List<Test> tests = mongoTemplate.find(searchQuery, Test.class);
//        List<Test> finalTestList = new ArrayList<>();
//        for (Test test : tests){
//            if(test.getApproved()){
//                finalTestList.add(test);
//            }else {
//                if(test.getReviewers().contains(userId) || test.getCreatedBy().equals(userId)){
//                    finalTestList.add(test);
//                }
//            }
//        }
//        tests = finalTestList;
        if (!sortKey.isEmpty()) {
            Comparator<Test> testComparator = Comparator.comparing(test -> {
                try {
                    Field field = Test.class.getDeclaredField(sortKey);
                    field.setAccessible(true);
                    Object value = field.get(test);
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
                tests.sort(testComparator.reversed());
            } else {
                tests.sort(testComparator);
            }
        }

        return tests;
    }

    public Test getTestById(String testId) {
        return testRepo.findById(testId).orElse(null);
    }

    public Boolean deleteTestById(String testId) {
        Test test = testRepo.findById(testId).orElse(null);
        if (test != null) {
            test.setDeleted(true);
            testRepo.save(test);
            return true;
        } else {
            return false;
        }
    }

    public Test updateTest(TestDto testDto, String testId) {
        Test test = testRepo.findById(testId).orElse(null);
        if (test != null) {
            if (testDto.getMilestones() != null) {
                List<Phase<Task>> phases = new ArrayList<>();
                int i = 0;
                for (Phase<Task> coursePhase : test.getMilestones()) {
                    if (i < testDto.getMilestones().size()) {
                        List<Task> taskList = testDto.getMilestones().get(i);
                        int j = 0;
                        List<Task> tasks = new ArrayList<>();
                        for (Task task : coursePhase.getTasks()) {
                            if (j < taskList.size()) {
                                Task taskOfCourseDto = taskList.get(j);
                                List<SubTask> subTasksOfDto = taskOfCourseDto.getSubtasks();
                                List<SubTask> subTasks = new ArrayList<>();
                                task.setMainTask(taskOfCourseDto.getMainTask());
                                int k = 0;
                                for (SubTask subTask : task.getSubtasks()) {
                                    if (k < subTasksOfDto.size()) {
                                        SubTask subTaskOfDto = subTasksOfDto.get(k);
                                        subTask.setSubTask(subTaskOfDto.getSubTask());
                                        subTask.setEstimatedTime(subTaskOfDto.getEstimatedTime());
                                        subTask.setLink(subTaskOfDto.getLink());
                                        subTasks.add(subTaskRepo.save(subTask));
                                    }
                                    k++;
                                }
                                while (k < subTasksOfDto.size()) {
                                    SubTask subTask = SubTask.builder()
                                            .entityType(EntityType.COURSE)
                                            .subTask(subTasksOfDto.get(k).getSubTask())
                                            .link(subTasksOfDto.get(k).getLink())
                                            .task(task)
                                            .build();
                                    subTask.setEstimatedTime(subTasksOfDto.get(k).getEstimatedTime());
                                    subTasks.add(subTaskRepo.save(subTask));
                                    k++;
                                }
                                task.setSubtasks(subTasks);
                                tasks.add(taskRepo.save(task));
                            }
                            j++;
                        }
                        while (j < taskList.size()) {
                            Task task = taskList.get(j);
                            task.set_id(String.valueOf(new ObjectId()));
                            List<SubTask> subTasks = new ArrayList<>();
                            for (SubTask subTask : task.getSubtasks()) {
                                subTask.setEntityType(EntityType.COURSE);
                                subTask.setTask(task);
                                subTasks.add(subTaskRepo.save(subTask));
                            }
                            task.setEntityType(EntityType.COURSE);
                            task.setSubtasks(subTasks);
                            task.setPhase(coursePhase);
                            tasks.add(taskRepo.save(task));

                            j++;
                        }
                        coursePhase.setTasks(tasks);
                        phases.add(phaseRepo.save(coursePhase));
                    }
                    i++;
                }
                while (i < testDto.getMilestones().size()) {
                    Phase<Task> phase = new Phase<>();
                    phase.set_id(String.valueOf(new ObjectId()));
                    List<Task> tasks = new ArrayList<>();
                    List<Task> courseDtoTasks = testDto.getMilestones().get(i);
                    for (Task task : courseDtoTasks) {
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
                    phase.setName("Phase " + i);
                    phase.setEntityType(EntityType.COURSE);
                    phase.setTasks(tasks);
                    phase.setEntity(test);
                    phases.add(phaseRepo.save(phase));

                    i++;
                }
                test.setMilestones(phases);
//                List<Phase<Task>> milestones = new ArrayList<>();
//                int i = 0, j = 0;
//                System.out.println("TEst Phase size = " + test.getMilestones().size());
//                System.out.println("TEstDto Phase size = " + testDto.getMilestones().size());
//
//                while(i < test.getMilestones().size() && j < testDto.getMilestones().size()){
//                    Phase milestone = test.getMilestones().get(i);
//                    milestone.setTasks(testDto.getMilestones().get(j));
//                    i++;
//                    j++;
//                    milestones.add(milestone);
//                }
////                while(i < course.getPhases().size()){
////                    phases.add(course.getPhases().get(i));
////                    i++;
////                }
//                while(j < testDto.getMilestones().size()){
//                    Phase<Task> milestone = Phase.<Task>builder()
//                            ._id(String.valueOf(new ObjectId()))
//                            .tasks(testDto.getMilestones().get(j))
//                            .build();
//                    milestones.add(milestone);
//                    j++;
//                }
//                test.setMilestones(milestones);
//                for (List<TestTask> testTasks : testDto.getMilestones()) {
//                    Milestone milestone = Milestone.builder()
//                            .tasks(testTasks)
//                            .build();
//                    milestones.add(milestone);
//                }
            }
            // Only update properties from the DTO if they are not null
            if (testDto.getTestName() != null) {
                test.setTestName(testDto.getTestName());
            }
            if (testDto.getApprover() != null) {
                test.setApprover(testDto.getApprover());
                Integer count = 0;
                for (String approver : test.getApprover()){
                    if(test.getApprovedBy().contains(approver)){
                        count++;
                    }
                }
                if(count == test.getApprover().size()){
                    test.setApproved(true);
                }else {
                    test.setApproved(false);
                }
                Set<String> approvedBy = new HashSet<>();
                for (String approver : test.getApprovedBy()){
                    if(test.getApprover().contains(approver)){
                        approvedBy.add(approver);
                    }
                }
                test.setApprovedBy(approvedBy);
            }
            if (testDto.getTeams() != null) {
                test.setTeams(testDto.getTeams());
            }

            // Saving the updated test
            testRepo.save(test);
            return test;
        } else {
            return null;
        }
    }

    public long countNonDeletedTests(String query) {
        MatchOperation matchStage = Aggregation.match(Criteria.where("testName").regex(query, "i")
                .and("deleted").is(false));

        Aggregation aggregation = Aggregation.newAggregation(matchStage);
        AggregationResults<Test> aggregationResults = mongoTemplate.aggregate(aggregation, "test", Test.class);
        return aggregationResults.getMappedResults().size();
    }

    public Test approve(Test test, String userId) {
        Set<String> approvedBy = test.getApprovedBy();
        approvedBy.add(userId);
        test.setApprovedBy(approvedBy);
        if (test.getApprover().size() == approvedBy.size()) {
            test.setApproved(true);
        } else {
            test.setApproved(false);
        }
        return testRepo.save(test);
    }

    public List<Phase> getTestByMilestoneIds(String testId, List<Object> milestoneIds) {
        List<String> milestonesIds = milestoneIds.stream().map(Object::toString).collect(Collectors.toList());
        System.out.println("Test " + milestoneIds);
        Query testQuery = new Query(Criteria.where("_id").is(testId).and("milestones._id").in(milestonesIds));
        Test test = mongoTemplate.findOne(testQuery, Test.class);
        System.out.println(test);
        if (test != null) {
            List<Phase> milestones = test.getMilestones().stream()
                    .filter(milestone -> milestoneIds.contains(milestone.get_id()))
                    .collect(Collectors.toList());
            return milestones;
        } else {
            return Collections.emptyList();
        }
    }
    public List<Map<String,String>> findTestsByIds(List<String> Ids){
        Criteria criteria = Criteria.where("_id").in(Ids);
        Query query = new Query(criteria);
        query.fields().include("_id","name","milestones._id","milestones.name");
        List<Course> course = mongoTemplate.find(new Query(criteria),Course.class);
//        HashMap<String,String> courseDetails = new HashMap<>();
        List<Map<String,String>> testDetailsList = Arrays.asList(new HashMap<>(),new HashMap<>());
        course.forEach(c -> {
            testDetailsList.get(0).put(c.get_id(),c.getName());
            c.getPhases().forEach(p -> {
                testDetailsList.get(1).put(p.get_id(),p.getName());
            });
        });
        return testDetailsList;
    }
}
