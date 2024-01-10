package com.chicmic.trainingModule.Service.PlanServices;

import com.chicmic.trainingModule.Dto.PlanDto.PlanDto;
import com.chicmic.trainingModule.Dto.UserIdAndNameDto;
import com.chicmic.trainingModule.Entity.*;
import com.chicmic.trainingModule.Entity.Constants.EntityType;
import com.chicmic.trainingModule.Repository.PhaseRepo;
import com.chicmic.trainingModule.Repository.PlanRepo;
import com.chicmic.trainingModule.Repository.PlanTaskRepo;
import com.chicmic.trainingModule.Service.CourseServices.CourseService;
import com.chicmic.trainingModule.Service.PhaseService;
import com.chicmic.trainingModule.Service.TestServices.TestService;
import com.chicmic.trainingModule.Util.CustomObjectMapper;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
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
import java.util.*;

@Service
@RequiredArgsConstructor
public class PlanService {
    private final PlanRepo planRepo;
    private final PhaseRepo phaseRepo;
    private final PlanTaskRepo planTaskRepo;
    private final CourseService courseService;
    private final PhaseService phaseService;
    private final TestService testService;
    private final MongoTemplate mongoTemplate;

    public Plan createPlan(PlanDto planDto, Principal principal) {
        Plan plan = Plan.builder()
                ._id(String.valueOf(new ObjectId()))
                .build();
        List<Phase<PlanTask>> phases = phaseService.createPlanPhases(planDto.getPhases(), plan);
        plan.setPhases(phases);
        plan.setApproved(false);
        plan.setDeleted(false);
        plan.setDescription(planDto.getDescription());
        plan.setPlanName(planDto.getPlanName());
        plan.setCreatedBy(principal.getName());
        plan.setApprover(planDto.getApprover());
        plan.setCreatedAt(LocalDateTime.now());
        plan.setUpdatedAt(LocalDateTime.now());
        plan.setCreatedBy(principal.getName());
        plan = planRepo.save(plan);
        return plan;
    }

    public List<Plan> getAllPlans(String query, Integer sortDirection, String sortKey) {
//        Query searchQuery = new Query()
//                .addCriteria(Criteria.where("planName").regex(query, "i"))
//                .addCriteria(Criteria.where("deleted").is(false));
//
//        List<Plan> plans = mongoTemplate.find(searchQuery, Plan.class);
        Criteria criteria = Criteria.where("planName").regex(query, "i")
                .and("deleted").is(false);

        Criteria approvedCriteria = Criteria.where("approved").is(true);


        // Combining the conditions
        Criteria finalCriteria = new Criteria().andOperator(
                criteria,
                new Criteria().orOperator(approvedCriteria)
        );

        Query searchQuery = new Query(finalCriteria);

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

    public List<Plan> getAllPlans(Integer pageNumber, Integer pageSize, String query, Integer sortDirection, String sortKey, String userId) {
        Pageable pageable;
        if (!sortKey.isEmpty()) {
            Sort.Direction direction = (sortDirection == 0) ? Sort.Direction.ASC : Sort.Direction.DESC;
            Sort sort = Sort.by(direction, sortKey);
            pageable = PageRequest.of(pageNumber, pageSize, sort);
        } else {
            pageable = PageRequest.of(pageNumber, pageSize);
        }

//        Query searchQuery = new Query()
//                .addCriteria(Criteria.where("planName").regex(query, "i"))
//                .addCriteria(Criteria.where("deleted").is(false))
//                .with(pageable);
//
//        List<Plan> plans = mongoTemplate.find(searchQuery, Plan.class);
//
        Criteria criteria = Criteria.where("planName").regex(query, "i")
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
        if (planId == null) {
            return null;
        }
        Plan plan =  planRepo.findById(planId).orElse(null);
        return plan != null && plan.getDeleted() ? null : plan;
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
        System.out.println("PlanDto");
        Plan plan = planRepo.findById(planId).orElse(null);
        if (plan != null) {
            if (planDto.getPlanName() != null) {
                System.out.println("PlanDto Name = " + planDto.getPlanName());
                plan.setPlanName(planDto.getPlanName());
            }
            if (planDto.getApprover() != null) {
                System.out.println("IM approving");
                Integer count = 0;
                for (String reviewer : plan.getApprover()) {
                    if (plan.getApprovedBy().contains(reviewer)) {
                        count++;
                    }
                }
                if (count == plan.getApprover().size()) {
                    plan.setApproved(true);
                } else {
                    plan.setApproved(false);
                }
                Set<String> approvedBy = new HashSet<>();
                for (String approver : plan.getApprovedBy()) {
                    if (plan.getApprover().contains(approver)) {
                        approvedBy.add(approver);
                    }
                }
                plan.setApprovedBy(approvedBy);
                plan.setApprover(planDto.getApprover());
            }
            if (planDto.getDescription() != null) {
                plan.setDescription(planDto.getDescription());
            }
            if (planDto.getPhases() != null) {
                List<Phase<PlanTask>> phases = phaseService.createPlanPhases(planDto.getPhases(), plan);
                plan.setPhases(phases);
            }
        plan.setUpdatedAt(LocalDateTime.now());
        planRepo.save(plan);
        return plan;
    } else

    {
        return null;
    }

}

    public HashMap<String, List<UserIdAndNameDto>> getPlanCourseByPlanIds(List<String> planIds) {
        Query searchQuery = new Query(Criteria.where("_id").in(planIds).and("phases.tasks.planType").is(1));
        List<Plan> plans = mongoTemplate.find(searchQuery, Plan.class);
        HashMap<String, List<UserIdAndNameDto>> courseIds = new HashMap<>();

        for (Plan plan : plans) {
            for (Phase<PlanTask> phase : plan.getPhases()) {
                for (PlanTask planTask : phase.getTasks()) {
                    if (planTask.getPlanType() == 1) {
                        UserIdAndNameDto course = new UserIdAndNameDto();
                        course.set_id((String) planTask.getPlan());
                        course.setName(courseService.getCourseById((String) planTask.getPlan()).getName());

                        courseIds.putIfAbsent(plan.get_id(), new ArrayList<>());
                        courseIds.get(plan.get_id()).add(course);
                    }
                }
            }
        }
        return courseIds;
    }


    public long countNonDeletedPlans(String query, String userId) {
        Criteria criteria = Criteria.where("planName").regex(query, "i")
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
        return mongoTemplate.aggregate(aggregation, "plan", Plan.class).getMappedResults().size();
    }

    public Plan approve(Plan plan, String userId) {
        Set<String> approvedBy = plan.getApprovedBy();
        approvedBy.add(userId);
        plan.setApprovedBy(approvedBy);
        if (plan.getApprover().size() == approvedBy.size()) {
            plan.setApproved(true);
        } else {
            plan.setApproved(false);
        }
        return planRepo.save(plan);
    }
}
