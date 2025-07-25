package com.emenu.features.subscription.service.impl;

import com.emenu.enums.sub_scription.SubscriptionPlanStatus;
import com.emenu.features.subscription.dto.filter.SubscriptionPlanFilterRequest;
import com.emenu.features.subscription.dto.request.SubscriptionPlanCreateRequest;
import com.emenu.features.subscription.dto.response.SubscriptionPlanResponse;
import com.emenu.features.subscription.dto.update.SubscriptionPlanUpdateRequest;
import com.emenu.features.subscription.mapper.SubscriptionPlanMapper;
import com.emenu.features.subscription.models.SubscriptionPlan;
import com.emenu.features.subscription.repository.SubscriptionPlanRepository;
import com.emenu.features.subscription.repository.SubscriptionRepository;
import com.emenu.features.subscription.service.SubscriptionPlanService;
import com.emenu.features.subscription.specification.SubscriptionPlanSpecification;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.pagination.PaginationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SubscriptionPlanServiceImpl implements SubscriptionPlanService {

    private final SubscriptionPlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanMapper planMapper;

    @Override
    public SubscriptionPlanResponse createPlan(SubscriptionPlanCreateRequest request) {
        log.info("Creating subscription plan: {}", request.getName());

        // Check if plan with same name already exists
        if (planRepository.existsByNameAndIsDeletedFalse(request.getName())) {
            throw new RuntimeException("Plan with this name already exists: " + request.getName());
        }

        SubscriptionPlan plan = planMapper.toEntity(request);
        SubscriptionPlan savedPlan = planRepository.save(plan);

        log.info("Subscription plan created successfully: {} with ID: {}", savedPlan.getName(), savedPlan.getId());
        return planMapper.toResponse(savedPlan);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<SubscriptionPlanResponse> getAllPlans(SubscriptionPlanFilterRequest filter) {
        log.debug("Getting subscription plans with filter - Status: {}, Search: {}, PublicOnly: {}",
                filter.getStatus(), filter.getSearch(), filter.getPublicOnly());

        Specification<SubscriptionPlan> spec = SubscriptionPlanSpecification.buildSpecification(filter);

        int pageNo = filter.getPageNo() != null && filter.getPageNo() > 0 ? filter.getPageNo() - 1 : 0;
        Pageable pageable = PaginationUtils.createPageable(
                pageNo, filter.getPageSize(), filter.getSortBy(), filter.getSortDirection()
        );

        Page<SubscriptionPlan> planPage = planRepository.findAll(spec, pageable);
        return planMapper.toPaginationResponse(planPage);
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionPlanResponse getPlanById(UUID planId) {
        log.debug("Getting subscription plan by ID: {}", planId);

        SubscriptionPlan plan = planRepository.findByIdAndIsDeletedFalse(planId)
                .orElseThrow(() -> new RuntimeException("Subscription plan not found with ID: " + planId));

        return planMapper.toResponse(plan);
    }

    @Override
    public SubscriptionPlanResponse updatePlan(UUID planId, SubscriptionPlanUpdateRequest request) {
        log.info("Updating subscription plan: {}", planId);

        SubscriptionPlan plan = planRepository.findByIdAndIsDeletedFalse(planId)
                .orElseThrow(() -> new RuntimeException("Subscription plan not found with ID: " + planId));

        // Check if name is being changed and if new name already exists
        if (request.getName() != null && !request.getName().equals(plan.getName())) {
            if (planRepository.existsByNameAndIsDeletedFalse(request.getName())) {
                throw new RuntimeException("Plan with name '" + request.getName() + "' already exists");
            }
        }

        planMapper.updateEntity(request, plan);
        SubscriptionPlan updatedPlan = planRepository.save(plan);

        log.info("Subscription plan updated successfully: {} - {}", updatedPlan.getId(), updatedPlan.getName());
        return planMapper.toResponse(updatedPlan);
    }

    @Override
    public void deletePlan(UUID planId) {
        log.info("Deleting subscription plan: {}", planId);

        SubscriptionPlan plan = planRepository.findByIdAndIsDeletedFalse(planId)
                .orElseThrow(() -> new RuntimeException("Subscription plan not found with ID: " + planId));

        // Check if plan is currently in use
        if (!canDeletePlan(planId)) {
            throw new RuntimeException("Cannot delete plan that is currently in use by active subscriptions");
        }

        plan.softDelete();
        planRepository.save(plan);

        log.info("Subscription plan deleted successfully: {} - {}", plan.getId(), plan.getName());
    }

    @Override
    public void seedDefaultPlans() {
        log.info("Seeding default subscription plans");

        // Check if plans already exist
        if (planRepository.count() > 0) {
            log.info("Plans already exist ({}), skipping seed", planRepository.count());
            return;
        }

        try {
            // Create default plans with simplified structure
            createDefaultPlan("Free Plan",
                    "Basic plan to get started with essential features",
                    BigDecimal.ZERO, 30, SubscriptionPlanStatus.PUBLIC);

            createDefaultPlan("Basic Plan",
                    "Perfect for small restaurants with standard features",
                    new BigDecimal("9.99"), 30, SubscriptionPlanStatus.PUBLIC);

            log.info("Default subscription plans seeded successfully");

        } catch (Exception e) {
            log.error("Error seeding default subscription plans: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to seed default subscription plans", e);
        }
    }

    @Transactional(readOnly = true)
    private boolean canDeletePlan(UUID planId) {
        return !isPlanInUse(planId);
    }

    @Transactional(readOnly = true)
    private boolean isPlanInUse(UUID planId) {
        long subscriptionCount = subscriptionRepository.countByPlan(planId);
        log.debug("Plan {} has {} subscriptions", planId, subscriptionCount);
        return subscriptionCount > 0;
    }

    // Private helper methods

    private void createDefaultPlan(String name, String description, BigDecimal price,
                                   Integer durationDays, SubscriptionPlanStatus status) {
        try {
            // Check if plan with this name already exists
            if (planRepository.existsByNameAndIsDeletedFalse(name)) {
                log.debug("Plan '{}' already exists, skipping creation", name);
                return;
            }

            SubscriptionPlan plan = new SubscriptionPlan();
            plan.setName(name);
            plan.setDescription(description);
            plan.setPrice(price);
            plan.setDurationDays(durationDays);
            plan.setStatus(status);

            SubscriptionPlan saved = planRepository.save(plan);
            log.debug("Created default plan: {} with ID: {}", name, saved.getId());

        } catch (Exception e) {
            log.error("Failed to create default plan '{}': {}", name, e.getMessage(), e);
            throw new RuntimeException("Failed to create default plan: " + name, e);
        }
    }
}