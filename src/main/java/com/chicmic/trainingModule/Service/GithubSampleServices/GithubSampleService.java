package com.chicmic.trainingModule.Service.GithubSampleServices;

import com.chicmic.trainingModule.Dto.GithubSampleDto.GithubSampleDto;
import com.chicmic.trainingModule.Entity.Session;
import com.chicmic.trainingModule.Repository.GithubSampleRepo;
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
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

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
        githubSample = githubSampleRepo.save(githubSample);
        return githubSample;
    }

    public List<GithubSample> getAllGithubSamples(Integer pageNumber, Integer pageSize, String query, Integer sortDirection, String sortKey) {
        System.out.println("pageNumber = " + pageNumber);
        System.out.println("pageSize = " + pageSize);
        System.out.println("query = " + query);
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        Query searchQuery = new Query()
                .addCriteria(Criteria.where("projectName").regex(query, "i"))
                .addCriteria(Criteria.where("isDeleted").is(false))
                .with(pageable);

        List<GithubSample> githubSamples = mongoTemplate.find(searchQuery, GithubSample.class);
        if(!sortKey.isEmpty()) {
            Comparator<GithubSample> githubSampleComparator = Comparator.comparing(githubSample -> {
                try {
                    Field field = GithubSample.class.getDeclaredField(sortKey);
                    field.setAccessible(true);
                    Object value = field.get(githubSample);
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
                githubSamples.sort(githubSampleComparator.reversed());
            } else {
                githubSamples.sort(githubSampleComparator);
            }
        }
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
            githubSampleRepo.save(githubSample);
            return githubSample;
        } else {
            return null;
        }
    }

    public long countNonDeletedGithubSamples() {
        MatchOperation matchStage = Aggregation.match(Criteria.where("isDeleted").is(false));
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
