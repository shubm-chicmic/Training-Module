package com.chicmic.trainingModule.Service.PlanServices;

import com.chicmic.trainingModule.Dto.PlanDto.PlanDto;
import com.chicmic.trainingModule.Entity.Plan.Plan;

import com.chicmic.trainingModule.Entity.Session.Session;
import com.chicmic.trainingModule.Repository.PlanRepo;
import com.chicmic.trainingModule.Util.CustomObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PlanService {
    private final PlanRepo planRepo;
    private final MongoTemplate mongoTemplate;

    public Plan createPlan(Plan plan, Principal principal) {
        plan.setCreatedAt(LocalDateTime.now());
        plan.setUpdatedAt(LocalDateTime.now());
        plan.setCreatedBy(principal.getName());
        plan = planRepo.save(plan);
        return plan;
    }

    public List<Plan> getAllPlans(String query, Integer sortDirection, String sortKey) {
        Query searchQuery = new Query()
                .addCriteria(Criteria.where("planName").regex(query, "i"))
                .addCriteria(Criteria.where("deleted").is(false));

        List<Plan> plans = mongoTemplate.find(searchQuery, Plan.class);

        if (!sortKey.isEmpty()) {
            Comparator<Plan> planComparator = Comparator.comparing(plan -> {
                try {
                    Field field = Plan.class.getDeclaredField(sortKey);
                    field.setAccessible(true);
                    Object value = field.get(plan);
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
                plans.sort(planComparator.reversed());
            } else {
                plans.sort(planComparator);
            }
        }

        return plans;
    }

    public List<Plan> getAllPlans(Integer pageNumber, Integer pageSize, String query, Integer sortDirection, String sortKey) {
        Pageable pageable;
        if (!sortKey.isEmpty()) {
            Sort.Direction direction = (sortDirection == 0) ? Sort.Direction.ASC : Sort.Direction.DESC;
            Sort sort = Sort.by(direction, sortKey);
            pageable = PageRequest.of(pageNumber, pageSize, sort);
        } else {
            pageable = PageRequest.of(pageNumber, pageSize);
        }

        Query searchQuery = new Query()
                .addCriteria(Criteria.where("planName").regex(query, "i"))
                .addCriteria(Criteria.where("deleted").is(false))
                .with(pageable);

        List<Plan> plans = mongoTemplate.find(searchQuery, Plan.class);

        if (!sortKey.isEmpty()) {
            Comparator<Plan> planComparator = Comparator.comparing(plan -> {
                try {
                    Field field = Plan.class.getDeclaredField(sortKey);
                    field.setAccessible(true);
                    Object value = field.get(plan);
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
                plans.sort(planComparator.reversed());
            } else {
                plans.sort(planComparator);
            }
        }

        return plans;
    }

    public Plan getPlanById(String planId) {
        return planRepo.findById(planId).orElse(null);
    }
    public List<Plan> getPlanByIds(List<String> planIds) {
        System.out.println("plans  +  " + planIds);
        return planRepo.findAllById(planIds);
    }

    public Boolean deletePlanById(String planId) {
        Plan plan = planRepo.findById(planId).orElse(null);
        if (plan != null) {
            plan.setDeleted(true);
            planRepo.save(plan);
            return true;
        } else {
            return false;
        }
    }

    public Plan updatePlan(PlanDto planDto, String planId) {
        Plan plan = planRepo.findById(planId).orElse(null);
        if (plan != null) {
            plan = (Plan) CustomObjectMapper.updateFields(planDto, plan);
            plan.setUpdatedAt(LocalDateTime.now());
            planRepo.save(plan);
            return plan;
        } else {
            return null;
        }

    }

    public long countNonDeletedPlans() {
        MatchOperation matchStage = Aggregation.match(Criteria.where("deleted").is(false));
        Aggregation aggregation = Aggregation.newAggregation(matchStage);
        return mongoTemplate.aggregate(aggregation, "plan", Plan.class).getMappedResults().size();
    }

    public Plan approve(Plan plan, String userId) {
        Set<String> approvedBy = plan.getApprovedBy();
        approvedBy.add(userId);
        plan.setApprovedBy(approvedBy);
        if (plan.getReviewers().size() == approvedBy.size()) {
            plan.setApproved(true);
        } else {
            plan.setApproved(false);
        }
        return planRepo.save(plan);
    }
}