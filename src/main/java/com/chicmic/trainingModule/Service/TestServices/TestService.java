package com.chicmic.trainingModule.Service.TestServices;

import com.chicmic.trainingModule.Dto.TestDto.TestDto;
import com.chicmic.trainingModule.Entity.Course.Course;
import com.chicmic.trainingModule.Entity.Course.Phase;
import com.chicmic.trainingModule.Entity.Test.Milestone;
import com.chicmic.trainingModule.Entity.Test.Test;
import com.chicmic.trainingModule.Entity.Test.TestTask;
import com.chicmic.trainingModule.Repository.TestRepo;
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
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;

@Service
@RequiredArgsConstructor
public class TestService {
    private final TestRepo testRepo;
    private final MongoTemplate mongoTemplate;

    public Test createTest(Test test) {
        test = testRepo.save(test);
        return test;
    }

    public List<Test> getAllTests(String query, Integer sortDirection, String sortKey) {
        Query searchQuery = new Query()
                .addCriteria(Criteria.where("testName").regex(query, "i"))
                .addCriteria(Criteria.where("deleted").is(false));

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

        Query searchQuery = new Query()
                .addCriteria(Criteria.where("testName").regex(query, "i"))
                .addCriteria(Criteria.where("deleted").is(false))
                .with(pageable);

        List<Test> tests = mongoTemplate.find(searchQuery, Test.class);
        List<Test> finalTestList = new ArrayList<>();
        for (Test test : tests){
            if(test.getApproved()){
                finalTestList.add(test);
            }else {
                if(test.getReviewers().contains(userId) || test.getCreatedBy().equals(userId)){
                    finalTestList.add(test);
                }
            }
        }
        tests = finalTestList;
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
            List<Milestone> milestones = new ArrayList<>();
            if (testDto.getMilestones() != null) {
                for (List<TestTask> testTasks : testDto.getMilestones()) {
                    Milestone milestone = Milestone.builder()
                            .tasks(testTasks)
                            .build();
                    milestones.add(milestone);
                }
            }
            // Only update properties from the DTO if they are not null
            if (testDto.getTestName() != null) {
                test.setTestName(testDto.getTestName());
            }
            if (testDto.getReviewers() != null) {
                test.setReviewers(testDto.getReviewers());
            }
            if (testDto.getTeams() != null) {
                test.setTeams(testDto.getTeams());
            }
            if (!milestones.isEmpty()) {
                test.setMilestones(milestones);
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
        if (test.getReviewers().size() == approvedBy.size()) {
            test.setApproved(true);
        } else {
            test.setApproved(false);
        }
        return testRepo.save(test);
    }

    public List<Milestone> getTestByMilestoneIds(String testId, List<Object> milestoneIds) {
        List<String> milestonesIds = milestoneIds.stream().map(Object::toString).collect(Collectors.toList());
        System.out.println("Test " + milestoneIds);
        Query testQuery = new Query(Criteria.where("_id").is(testId).and("milestones._id").in(milestonesIds));
        Test test = mongoTemplate.findOne(testQuery, Test.class);
        System.out.println(test);
        if (test != null) {
            List<Milestone> milestones = test.getMilestones().stream()
                    .filter(milestone -> milestoneIds.contains(milestone.get_id()))
                    .collect(Collectors.toList());
            return milestones;
        } else {
            return Collections.emptyList();
        }
    }
}
