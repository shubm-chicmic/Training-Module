//package com.chicmic.trainingModule.Service.TestServices;
//
//import com.chicmic.trainingModule.Dto.TestDto.TestDto;
//import com.chicmic.trainingModule.Entity.Course123.Phase;
//import com.chicmic.trainingModule.Entity.Test.Milestone;
//import com.chicmic.trainingModule.Entity.Test;
//import com.chicmic.trainingModule.Repository.TestRepo;
//import lombok.RequiredArgsConstructor;
//import org.bson.types.ObjectId;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.data.mongodb.core.MongoTemplate;
//import org.springframework.data.mongodb.core.aggregation.Aggregation;
//import org.springframework.data.mongodb.core.aggregation.AggregationResults;
//import org.springframework.data.mongodb.core.aggregation.MatchOperation;
//import org.springframework.data.mongodb.core.query.Criteria;
//import org.springframework.data.mongodb.core.query.Query;
//import org.springframework.stereotype.Service;
//
//import java.lang.reflect.Field;
//import java.time.LocalDateTime;
//import java.util.*;
//import java.util.stream.Collectors;
//
//import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
//import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
//
//@Service
//@RequiredArgsConstructor
//public class TestService {
//    private final TestRepo testRepo;
//    private final MongoTemplate mongoTemplate;
//
//    public Test createTest(Test test) {
//        test.setCreatedAt(LocalDateTime.now());
//        test.setUpdatedAt(LocalDateTime.now());
//        test = testRepo.save(test);
//        return test;
//    }
//
//    public List<Test> getAllTests(String query, Integer sortDirection, String sortKey) {
//        Criteria criteria = Criteria.where("testName").regex(query, "i")
//                .and("deleted").is(false);
//
//        Criteria approvedCriteria = Criteria.where("approved").is(true);
//
//
//        // Combining the conditions
//        Criteria finalCriteria = new Criteria().andOperator(
//                criteria,
//                new Criteria().orOperator(approvedCriteria)
//        );
//
//        Query searchQuery = new Query(finalCriteria);
//
//        List<Test> tests = mongoTemplate.find(searchQuery, Test.class);
//
//        if (!sortKey.isEmpty()) {
//            Comparator<Test> testComparator = Comparator.comparing(test -> {
//                try {
//                    Field field = Test.class.getDeclaredField(sortKey);
//                    field.setAccessible(true);
//                    Object value = field.get(test);
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
//                tests.sort(testComparator.reversed());
//            } else {
//                tests.sort(testComparator);
//            }
//        }
//
//        return tests;
//    }
//    public List<Test> getAllTests(Integer pageNumber, Integer pageSize, String query, Integer sortDirection, String sortKey, String userId) {
//        Pageable pageable;
//        if (!sortKey.isEmpty()) {
//            Sort.Direction direction = (sortDirection == 0) ? Sort.Direction.ASC : Sort.Direction.DESC;
//            Sort sort = Sort.by(direction, sortKey);
//            pageable = PageRequest.of(pageNumber, pageSize, sort);
//        } else {
//            pageable = PageRequest.of(pageNumber, pageSize);
//        }
//        Criteria criteria = Criteria.where("testName").regex(query, "i")
//                .and("deleted").is(false);
//
//        Criteria approvedCriteria = Criteria.where("approved").is(true);
//        Criteria reviewersCriteria = Criteria.where("approved").is(false)
//                .and("reviewers").in(userId);
//        Criteria createdByCriteria = Criteria.where("approved").is(false)
//                .and("createdBy").is(userId);
//
//        Criteria finalCriteria = new Criteria().andOperator(
//                criteria,
//                new Criteria().orOperator(approvedCriteria, reviewersCriteria, createdByCriteria)
//        );
//        Query searchQuery = new Query(finalCriteria).with(pageable);
////        Query searchQuery = new Query()
////                .addCriteria(Criteria.where("testName").regex(query, "i"))
////                .addCriteria(Criteria.where("deleted").is(false))
////                .with(pageable);
//
//        List<Test> tests = mongoTemplate.find(searchQuery, Test.class);
////        List<Test> finalTestList = new ArrayList<>();
////        for (Test test : tests){
////            if(test.getApproved()){
////                finalTestList.add(test);
////            }else {
////                if(test.getReviewers().contains(userId) || test.getCreatedBy().equals(userId)){
////                    finalTestList.add(test);
////                }
////            }
////        }
////        tests = finalTestList;
//        if (!sortKey.isEmpty()) {
//            Comparator<Test> testComparator = Comparator.comparing(test -> {
//                try {
//                    Field field = Test.class.getDeclaredField(sortKey);
//                    field.setAccessible(true);
//                    Object value = field.get(test);
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
//                tests.sort(testComparator.reversed());
//            } else {
//                tests.sort(testComparator);
//            }
//        }
//
//        return tests;
//    }
//
//    public Test getTestById(String testId) {
//        return testRepo.findById(testId).orElse(null);
//    }
//
//    public Boolean deleteTestById(String testId) {
//        Test test = testRepo.findById(testId).orElse(null);
//        if (test != null) {
//            test.setDeleted(true);
//            testRepo.save(test);
//            return true;
//        } else {
//            return false;
//        }
//    }
//
//    public Test updateTest(TestDto testDto, String testId) {
//        Test test = testRepo.findById(testId).orElse(null);
//        if (test != null) {
//            if (testDto.getMilestones() != null) {
//                List<Milestone> milestones = new ArrayList<>();
//                int i = 0, j = 0;
//                System.out.println("TEst Phase size = " + test.getMilestones().size());
//                System.out.println("TEstDto Phase size = " + testDto.getMilestones().size());
//
//                while(i < test.getMilestones().size() && j < testDto.getMilestones().size()){
//                    Milestone milestone = test.getMilestones().get(i);
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
//                    Milestone milestone = Milestone.builder()
//                            ._id(String.valueOf(new ObjectId()))
//                            .tasks(testDto.getMilestones().get(j))
//                            .build();
//                    milestones.add(milestone);
//                    j++;
//                }
//                test.setMilestones(milestones);
////                for (List<TestTask> testTasks : testDto.getMilestones()) {
////                    Milestone milestone = Milestone.builder()
////                            .tasks(testTasks)
////                            .build();
////                    milestones.add(milestone);
////                }
//            }
//            // Only update properties from the DTO if they are not null
//            if (testDto.getTestName() != null) {
//                test.setTestName(testDto.getTestName());
//            }
//            if (testDto.getReviewers() != null) {
//                test.setReviewers(testDto.getReviewers());
//                Integer count = 0;
//                for (String reviewer : test.getReviewers()){
//                    if(test.getApprovedBy().contains(reviewer)){
//                        count++;
//                    }
//                }
//                if(count == test.getReviewers().size()){
//                    test.setApproved(true);
//                }else {
//                    test.setApproved(false);
//                }
//                Set<String> approvedBy = new HashSet<>();
//                for (String approver : test.getApprovedBy()){
//                    if(test.getReviewers().contains(approver)){
//                        approvedBy.add(approver);
//                    }
//                }
//                test.setApprovedBy(approvedBy);
//            }
//            if (testDto.getTeams() != null) {
//                test.setTeams(testDto.getTeams());
//            }
//
//            // Saving the updated test
//            testRepo.save(test);
//            return test;
//        } else {
//            return null;
//        }
//    }
//
//    public long countNonDeletedTests(String query) {
//        MatchOperation matchStage = Aggregation.match(Criteria.where("testName").regex(query, "i")
//                .and("deleted").is(false));
//
//        Aggregation aggregation = Aggregation.newAggregation(matchStage);
//        AggregationResults<Test> aggregationResults = mongoTemplate.aggregate(aggregation, "test", Test.class);
//        return aggregationResults.getMappedResults().size();
//    }
//
//    public Test approve(Test test, String userId) {
//        Set<String> approvedBy = test.getApprovedBy();
//        approvedBy.add(userId);
//        test.setApprovedBy(approvedBy);
//        if (test.getReviewers().size() == approvedBy.size()) {
//            test.setApproved(true);
//        } else {
//            test.setApproved(false);
//        }
//        return testRepo.save(test);
//    }
//
//    public List<Milestone> getTestByMilestoneIds(String testId, List<Object> milestoneIds) {
//        List<String> milestonesIds = milestoneIds.stream().map(Object::toString).collect(Collectors.toList());
//        System.out.println("Test " + milestoneIds);
//        Query testQuery = new Query(Criteria.where("_id").is(testId).and("milestones._id").in(milestonesIds));
//        Test test = mongoTemplate.findOne(testQuery, Test.class);
//        System.out.println(test);
//        if (test != null) {
//            List<Milestone> milestones = test.getMilestones().stream()
//                    .filter(milestone -> milestoneIds.contains(milestone.get_id()))
//                    .collect(Collectors.toList());
//            return milestones;
//        } else {
//            return Collections.emptyList();
//        }
//    }
//}
