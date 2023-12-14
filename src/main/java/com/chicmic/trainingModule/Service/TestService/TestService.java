package com.chicmic.trainingModule.Service.TestService;

import com.chicmic.trainingModule.Dto.CourseDto.CourseDto;
import com.chicmic.trainingModule.Dto.TestDto.TestDto;
import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.chicmic.trainingModule.Entity.MomMessage;
import com.chicmic.trainingModule.Entity.Phase;
import com.chicmic.trainingModule.Repository.TestRepo;
import com.chicmic.trainingModule.Util.CustomObjectMapper;
import com.chicmic.trainingModule.Entity.Test;
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
public class TestService {
    private final TestRepo testRepo;
    private final MongoTemplate mongoTemplate;

    public Test createTest(Test test) {
        test = testRepo.save(test);
        return test;
    }

    public List<Test> getAllTests(Integer pageNumber, Integer pageSize, String query, Integer sortDirection, String sortKey) {
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

    public Test updateStatus(String testId, int status) {
        Test test = testRepo.findById(testId).orElse(null);
        if (test != null) {
            test.setStatus(status);
            testRepo.save(test);
            return test;
        } else {
            return null;
        }
    }

    public Test updateTest(TestDto testDto, String testId) {
        Test test = testRepo.findById(testId).orElse(null);
        if (test != null) {

            test = Test.builder()
                    ._id(testId)
                    .testName(testDto.getTestName())
                    .reviewers(testDto.getReviewers())
                    .milestones(testDto.getMilestones())
                    .approved(test.getApproved())
                    .deleted(test.getDeleted())
                    .createdBy(test.getCreatedBy())
                    .build();

            testRepo.save(test);
            return test;
        } else {
            return null;
        }
    }

    public long countNonDeletedTests() {
        MatchOperation matchStage = Aggregation.match(Criteria.where("isDeleted").is(false));
        Aggregation aggregation = Aggregation.newAggregation(matchStage);
        AggregationResults<Test> aggregationResults = mongoTemplate.aggregate(aggregation, "test", Test.class);
        return aggregationResults.getMappedResults().size();
    }

    public Test approve(Test test, String userId) {
        Set<String> approvedBy = test.getApprovedBy();
        approvedBy.add(userId);
        if (test.getReviewers().size() == approvedBy.size()) {
            test.setApproved(true);
            test.setStatus(2);
        } else {
            test.setApproved(false);
        }
        return testRepo.save(test);
    }
}
