package com.chicmic.trainingModule.Service.GitSampleServices;

import com.chicmic.trainingModule.Dto.GithubSampleDto;
import com.chicmic.trainingModule.Repository.GithubSampleRepo;
import com.chicmic.trainingModule.Entity.GithubSample;
import com.chicmic.trainingModule.Util.CustomObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class GithubSampleService {
    private final GithubSampleRepo githubSampleRepo;
//    @Bean
//    private void cleargithub(){
//        githubSampleRepo.deleteAll();
//    }

    public GithubSample createGithubSample(GithubSample githubSample) {
        githubSample = githubSampleRepo.save(githubSample);
        return githubSample;
    }

    public List<GithubSample> getAllGithubSamples(Integer pageNumber, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        List<GithubSample> githubSamples = githubSampleRepo.findAll(pageable).getContent();
        return githubSamples;
    }

    public GithubSample getGithubSampleById(Long githubSampleId) {
        return githubSampleRepo.findById(githubSampleId).orElse(null);
    }

    public Boolean deleteGithubSampleById(Long githubSampleId) {
        GithubSample githubSample = githubSampleRepo.findById(githubSampleId).orElse(null);
        if (githubSample != null) {
            githubSample.setIsDeleted(true);
            githubSampleRepo.save(githubSample);
            return true;
        } else {
            return false;
        }
    }

    public GithubSample updateGithubSample(GithubSampleDto githubSampleDto, Long githubSampleId) {
        GithubSample githubSample = githubSampleRepo.findById(githubSampleId).orElse(null);
        if (githubSample != null) {
            githubSample = (GithubSample) CustomObjectMapper.updateFields(githubSampleDto, githubSample);
            githubSampleRepo.save(githubSample);
            return githubSample;
        } else {
            return null;
        }
    }

}