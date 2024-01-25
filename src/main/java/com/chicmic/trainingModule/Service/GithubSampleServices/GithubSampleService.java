package com.chicmic.trainingModule.Service.GithubSampleServices;

import com.chicmic.trainingModule.Dto.GithubSampleDto.GithubSampleDto;
import com.chicmic.trainingModule.Dto.UserDto;
import com.chicmic.trainingModule.Repository.GithubSampleRepo;
import com.chicmic.trainingModule.TrainingModuleApplication;
import com.chicmic.trainingModule.Util.CustomObjectMapper;
import com.chicmic.trainingModule.Entity.GithubSample;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Collation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class GithubSampleService {
    private final GithubSampleRepo githubSampleRepo;
    private final MongoTemplate mongoTemplate;
//    @Bean
//    private void clearSession(){
//        githubSampleRepo.deleteAll();
//    }

    public GithubSample createGithubSample(GithubSample githubSample){
        githubSample.setCreatedAt(LocalDateTime.now());
        githubSample.setUpdatedAt(LocalDateTime.now());
        githubSample = githubSampleRepo.save(githubSample);
        return githubSample;
    }

    public List<GithubSample> getAllGithubSamples(Integer pageNumber, Integer pageSize, String query, Integer sortDirection, String sortKey, String userId) {
        System.out.println("pageNumber = " + pageNumber);
        System.out.println("pageSize = " + pageSize);
        System.out.println("query = " + query);
        Pageable pageable;
        pageable = PageRequest.of(pageNumber, pageSize);

        UserDto userDto = TrainingModuleApplication.searchUserById(userId);
        List<String> teams = userDto.getTeams();
        System.out.println("userDto = " + userDto);
        System.out.println("teamId = " + teams);
        Criteria criteria = Criteria.where("projectName").regex(query, "i")
                .and("isDeleted").is(false);
        Criteria teamsCriteria = Criteria.where("teams").all(teams);
        Criteria approvedCriteria = Criteria.where("isApproved").is(true)
                .andOperator(
                        new Criteria().orOperator(
                                Criteria.where("createdBy").is(userId),
                                Criteria.where("approver").in(userId),
                                teamsCriteria
                        )
                );
        Criteria reviewersCriteria = Criteria.where("isApproved").is(false)
                .and("approver").in(userId);
        Criteria createdByCriteria = Criteria.where("isApproved").is(false)
                .and("createdBy").is(userId);

        Criteria finalCriteria = new Criteria().andOperator(
                criteria,
                new Criteria().orOperator(approvedCriteria, reviewersCriteria, createdByCriteria)
        );

        Collation collation = Collation.of(Locale.ENGLISH).strength(Collation.ComparisonLevel.secondary());

        Query searchQuery = new Query(finalCriteria).with(pageable).collation(collation).with(Sort.by(sortDirection == 1 ? Sort.Direction.ASC : Sort.Direction.DESC, sortKey));

        List<GithubSample> githubSamples = mongoTemplate.find(searchQuery, GithubSample.class);
//        if(!sortKey.isEmpty()) {
//            Comparator<GithubSample> githubSampleComparator = Comparator.comparing(githubSample -> {
//                try {
//                    Field field = GithubSample.class.getDeclaredField(sortKey);
//                    field.setAccessible(true);
//                    Object value = field.get(githubSample);
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
//                githubSamples.sort(githubSampleComparator.reversed());
//            } else {
//                githubSamples.sort(githubSampleComparator);
//            }
//        }
        return githubSamples;
    }

    public GithubSample getGithubSampleById(String githubSampleId){
        return githubSampleRepo.findById(githubSampleId).orElse(null);
    }

    public Boolean deleteGithubSampleById(String githubSampleId) {
        GithubSample githubSample = githubSampleRepo.findById(githubSampleId).orElse(null);
        if (githubSample != null) {
            githubSample.setIsDeleted(true);
            githubSampleRepo.save(githubSample);
            return true;
        } else {
            return false;
        }
    }

    public GithubSample updateGithubSample(GithubSampleDto githubSampleDto, String githubSampleId) {
        GithubSample githubSample = githubSampleRepo.findById(githubSampleId).orElse(null);
        if (githubSample != null) {
            githubSample = (GithubSample) CustomObjectMapper.updateFields(githubSampleDto, githubSample);
            Integer count = 0;
            for (String reviewer : githubSample.getApprover()){
                if(githubSample.getApprovedBy().contains(reviewer)){
                    count++;
                }
            }

            if(count > 0){
                githubSample.setIsApproved(true);
            }else {
                githubSample.setIsApproved(false);
            }

            Set<String> approvedBy = new HashSet<>();
            for (String approver : githubSample.getApprovedBy()){
                if(githubSample.getApprover().contains(approver)){
                    approvedBy.add(approver);
                }
            }
            githubSample.setApprovedBy(approvedBy);
            githubSample.setUpdatedAt(LocalDateTime.now());
            githubSampleRepo.save(githubSample);
            return githubSample;
        } else {
            return null;
        }
    }

    public long countNonDeletedGithubSamples(String query, String userId) {
        UserDto userDto = TrainingModuleApplication.searchUserById(userId);
        List<String> teams = userDto.getTeams();
        System.out.println("userDto = " + userDto);
        System.out.println("teamId = " + teams);
        Criteria criteria = Criteria.where("projectName").regex(query, "i")
                .and("isDeleted").is(false);
        Criteria teamsCriteria = Criteria.where("teams").all(teams);
        Criteria approvedCriteria = Criteria.where("isApproved").is(true)
                .andOperator(
                        new Criteria().orOperator(
                                Criteria.where("createdBy").is(userId),
                                Criteria.where("approver").in(userId),
                                teamsCriteria
                        )
                );
        Criteria reviewersCriteria = Criteria.where("isApproved").is(false)
                .and("approver").in(userId);
        Criteria createdByCriteria = Criteria.where("isApproved").is(false)
                .and("createdBy").is(userId);

        Criteria finalCriteria = new Criteria().andOperator(
                criteria,
                new Criteria().orOperator(approvedCriteria, reviewersCriteria, createdByCriteria)
        );



        MatchOperation matchStage = Aggregation.match(finalCriteria);

        Aggregation aggregation = Aggregation.newAggregation(matchStage);
        AggregationResults<GithubSample> aggregationResults = mongoTemplate.aggregate(aggregation, "githubSample", GithubSample.class);
        return aggregationResults.getMappedResults().size();
    }


    public GithubSample approve(GithubSample githubSample, String userId) {
        Set<String> approvedBy = githubSample.getApprovedBy();
        approvedBy.add(userId);
        githubSample.setApprovedBy(approvedBy);
        githubSample.setIsApproved(true);
        return githubSampleRepo.save(githubSample);
    }
}
