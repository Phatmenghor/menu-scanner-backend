package com.emenu.features.services.service.impl;

import com.emenu.exception.UserNotFoundException;
import com.emenu.features.services.dto.request.CreatePlanRequest;
import com.emenu.features.services.dto.request.UpdatePlanRequest;
import com.emenu.features.services.dto.response.PlanResponse;
import com.emenu.features.services.service.SubscriptionPlanService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SubscriptionPlanServiceImpl implements SubscriptionPlanService {

    private final SubscriptionPlanRepository planRepository;
    private final SubscriptionPlanMapper planMapper;

    @Override
    public PlanResponse createPlan(CreatePlanRequest request) {
        log.info("Creating subscription plan: {}", request.getName());

        if (planRepository.existsByNameAndIsDeletedFalse(request.getName())) {
            throw new ValidationException("Plan name already exists");
        }

        SubscriptionPlan plan = planMapper.toEntity(request);
        plan.setIsActive(true);

        SubscriptionPlan savedPlan = planRepository.save(plan);
        log.info("Subscription plan created: {}", savedPlan.getName());

        return planMapper.toResponse(savedPlan);
    }

    @Override
    public PlanResponse getPlan(UUID id) {
        SubscriptionPlan plan = planRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("Subscription plan not found"));

        return planMapper.toResponse(plan);
    }

    @Override
    public PlanResponse updatePlan(UUID id, UpdatePlanRequest request) {
        SubscriptionPlan plan = planRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("Subscription plan not found"));

        planMapper.updateEntity(request, plan);
        SubscriptionPlan savedPlan = planRepository.save(plan);

        log.info("Subscription plan updated: {}", savedPlan.getName());
        return planMapper.toResponse(savedPlan);
    }

    @Override
    public void deletePlan(UUID id) {
        SubscriptionPlan plan = planRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("Subscription plan not found"));

        plan.softDelete();
        planRepository.save(plan);

        log.info("Subscription plan deleted: {}", plan.getName());
    }

    @Override
    public void activatePlan(UUID id) {
        SubscriptionPlan plan = planRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("Subscription plan not found"));

        plan.setIsActive(true);
        planRepository.save(plan);

        log.info("Subscription plan activated: {}", plan.getName());
    }

    @Override
    public void deactivatePlan(UUID id) {
        SubscriptionPlan plan = planRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("Subscription plan not found"));

        plan.setIsActive(false);
        planRepository.save(plan);

        log.info("Subscription plan deactivated: {}", plan.getName());
    }

    @Override
    public List<PlanResponse> listPlans(Boolean activeOnly) {
        List<SubscriptionPlan> plans;
        if (Boolean.TRUE.equals(activeOnly)) {
            plans = planRepository.findByIsActiveTrueAndIsDeletedFalseOrderBySortOrder();
        } else {
            plans = planRepository.findByIsDeletedFalseOrderBySortOrder();
        }

        return plans.stream()
                .map(planMapper::toResponse)
                .toList();
    }
}
