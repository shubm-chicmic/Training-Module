package com.chicmic.trainingModule.Service.TestServices;

import com.chicmic.trainingModule.Dto.TestDto.TestDto;
import com.chicmic.trainingModule.Entity.*;
import com.chicmic.trainingModule.Entity.Constants.EntityType;
import com.chicmic.trainingModule.Repository.*;
import com.chicmic.trainingModule.Service.CourseServices.CourseService;
import com.chicmic.trainingModule.Service.PhaseService;
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
    private final PhaseService phaseService;
    private final CourseService courseService;

    public Test createTest(Test test) {
        test.setCreatedAt(LocalDateTime.now());
        test.setUpdatedAt(LocalDateTime.now());
        test.set_id(String.valueOf(new ObjectId()));
        List<Phase<Task>> milestones = phaseService.createPhases(test.getMilestones(), test, EntityType.TEST);
        test.setMilestones(milestones);
        test = testRepo.save(test);
        return test;
    }

    public List<Test> getAllTests(String query, Integer sortDirection, String sortKey, String traineeId) {
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
        System.out.println("Tests : " + tests.size());
        List<Test> testList = new ArrayList<>();
        if (traineeId != null && !traineeId.isEmpty()) {
            System.out.println("\u001B[33m traineeId is coming in test \u001B[0m" + traineeId);
            List<String> testIds = courseService.getCoursesAndTestsByTraineeId(traineeId, EntityType.TEST);
            if(testIds != null && testIds.size() > 0) {
                for (Test test : tests) {
                    if (testIds.contains(test.get_id())) {
                        testList.add(test);
                    }
                }
            }
            return testList;
        }
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
        Test test = testRepo.findById(testId).orElse(null);
        return test != null && test.getDeleted() ? null : test;
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
                List<Phase<Task>> milestones = phaseService.createPhases(testDto.getMilestones(), test, EntityType.TEST);
                test.setMilestones(milestones);
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

    public long countNonDeletedTests(String query, String userId) {
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
        MatchOperation matchStage = Aggregation.match(finalCriteria);

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
        List<Test> test = mongoTemplate.find(new Query(criteria),Test.class);
//        HashMap<String,String> courseDetails = new HashMap<>();
        List<Map<String,String>> testDetailsList = Arrays.asList(new HashMap<>(),new HashMap<>());
        test.forEach(t -> {
            testDetailsList.get(0).put(t.get_id(),t.getTestName());
            t.getMilestones().forEach(p -> {
                testDetailsList.get(1).put(p.get_id(),p.getName());
            });
        });
        return testDetailsList;
    }
}
