package com.emenu.features.auth.service.impl;

import com.emenu.features.auth.dto.request.SubscriptionPlanCreateRequest;
import com.emenu.features.auth.dto.response.SubscriptionPlanResponse;
import com.emenu.features.auth.dto.update.SubscriptionPlanUpdateRequest;
import com.emenu.features.auth.mapper.SubscriptionPlanMapper;
import com.emenu.features.auth.models.SubscriptionPlan;
import com.emenu.features.auth.repository.SubscriptionPlanRepository;
import com.emenu.features.auth.repository.SubscriptionRepository;
import com.emenu.features.auth.service.SubscriptionPlanService;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.pagination.PaginationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
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

        if (planRepository.existsByNameAndIsDeletedFalse(request.getName())) {
            throw new RuntimeException("Plan with this name already exists");
        }

        SubscriptionPlan plan = planMapper.toEntity(request);
        SubscriptionPlan savedPlan = planRepository.save(plan);

        log.info("Subscription plan created successfully: {}", savedPlan.getName());
        return planMapper.toResponse(savedPlan);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionPlanResponse> getAllPlans() {
        List<SubscriptionPlan> plans = planRepository.findByIsDeletedFalse(null).getContent();
        return planMapper.toResponseList(plans);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<SubscriptionPlanResponse> getAllPlans(int pageNo, int pageSize) {
        int page = pageNo > 0 ? pageNo - 1 : 0;
        Pageable pageable = PaginationUtils.createPageable(page, pageSize, "createdAt", "DESC");

        Page<SubscriptionPlan> planPage = planRepository.findByIsDeletedFalse(pageable);
        return planMapper.toPaginationResponse(planPage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionPlanResponse> getAllActivePlans() {
        List<SubscriptionPlan> plans = planRepository.findAllActivePlans();
        return planMapper.toResponseList(plans);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionPlanResponse> getPublicPlans() {
        List<SubscriptionPlan> plans = planRepository.findPublicPlans();
        return planMapper.toResponseList(plans);
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionPlanResponse getPlanById(UUID planId) {
        SubscriptionPlan plan = planRepository.findByIdAndIsDeletedFalse(planId)
                .orElseThrow(() -> new RuntimeException("Subscription plan not found"));

        return planMapper.toResponse(plan);
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionPlanResponse getPlanByName(String planName) {
        SubscriptionPlan plan = planRepository.findByNameAndIsDeletedFalse(planName)
                .orElseThrow(() -> new RuntimeException("Subscription plan not found"));

        return planMapper.toResponse(plan);
    }

    @Override
    public SubscriptionPlanResponse updatePlan(UUID planId, SubscriptionPlanUpdateRequest request) {
        SubscriptionPlan plan = planRepository.findByIdAndIsDeletedFalse(planId)
                .orElseThrow(() -> new RuntimeException("Subscription plan not found"));

        planMapper.updateEntity(request, plan);
        SubscriptionPlan updatedPlan = planRepository.save(plan);

        log.info("Subscription plan updated successfully: {}", updatedPlan.getName());
        return planMapper.toResponse(updatedPlan);
    }

    @Override
    public void deletePlan(UUID planId) {
        SubscriptionPlan plan = planRepository.findByIdAndIsDeletedFalse(planId)
                .orElseThrow(() -> new RuntimeException("Subscription plan not found"));

        if (!canDeletePlan(planId)) {
            throw new RuntimeException("Cannot delete plan that is currently in use");
        }

        plan.softDelete();
        planRepository.save(plan);
        log.info("Subscription plan deleted: {}", plan.getName());
    }

    @Override
    public SubscriptionPlanResponse createCustomPlan(UUID businessId, SubscriptionPlanCreateRequest request) {
        log.info("Creating custom subscription plan for business: {}", businessId);

        // Create the plan but mark it as custom
        SubscriptionPlan plan = planMapper.toEntity(request);
        plan.setIsCustom(true);
        plan.setName(request.getName() + "_" + businessId.toString().substring(0, 8));

        SubscriptionPlan savedPlan = planRepository.save(plan);
        log.info("Custom subscription plan created successfully: {}", savedPlan.getName());

        return planMapper.toResponse(savedPlan);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionPlanResponse> getCustomPlansForBusiness(UUID businessId) {
        // In a real implementation, you might have a business_specific_plans table
        // For now, we'll return empty list as custom plans would need additional database structure
        log.info("Getting custom plans for business: {}", businessId);
        return List.of();
    }

    @Override
    public void activatePlan(UUID planId) {
        SubscriptionPlan plan = planRepository.findByIdAndIsDeletedFalse(planId)
                .orElseThrow(() -> new RuntimeException("Subscription plan not found"));

        plan.setIsActive(true);
        planRepository.save(plan);
        log.info("Subscription plan activated: {}", plan.getName());
    }

    @Override
    public void deactivatePlan(UUID planId) {
        SubscriptionPlan plan = planRepository.findByIdAndIsDeletedFalse(planId)
                .orElseThrow(() -> new RuntimeException("Subscription plan not found"));

        plan.setIsActive(false);
        planRepository.save(plan);
        log.info("Subscription plan deactivated: {}", plan.getName());
    }

    @Override
    public SubscriptionPlanResponse setAsDefault(UUID planId) {
        // First, remove default from all other plans
        List<SubscriptionPlan> defaultPlans = planRepository.findByIsDefaultAndIsDeletedFalse(true);
        defaultPlans.forEach(plan -> plan.setIsDefault(false));
        planRepository.saveAll(defaultPlans);

        // Set the specified plan as default
        SubscriptionPlan plan = planRepository.findByIdAndIsDeletedFalse(planId)
                .orElseThrow(() -> new RuntimeException("Subscription plan not found"));

        plan.setIsDefault(true);
        SubscriptionPlan savedPlan = planRepository.save(plan);

        log.info("Subscription plan set as default: {}", plan.getName());
        return planMapper.toResponse(savedPlan);
    }

    @Override
    public void seedDefaultPlans() {
        log.info("Seeding default subscription plans");

        if (planRepository.count() > 0) {
            log.info("Plans already exist, skipping seed");
            return;
        }

        // Create Free Plan
        createDefaultPlan("FREE", "Free Plan", "Get started with basic features",
                BigDecimal.ZERO, 30, 1, 10, 2, true, true);

        // Create Basic Plan
        createDefaultPlan("BASIC", "Basic Plan", "Perfect for small restaurants",
                new BigDecimal("29.99"), 30, 3, 50, 10, false, false);

        // Create Professional Plan
        createDefaultPlan("PROFESSIONAL", "Professional Plan", "Advanced features for growing businesses",
                new BigDecimal("79.99"), 30, 10, 200, 25, false, false);

        // Create Enterprise Plan
        createDefaultPlan("ENTERPRISE", "Enterprise Plan", "Full-featured solution for large operations",
                new BigDecimal("199.99"), 30, -1, -1, -1, false, false);

        log.info("Default subscription plans seeded successfully");
    }

    @Override
    public void updateDefaultPlans() {
        log.info("Updating default subscription plans");
        // Implementation for updating existing default plans
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canDeletePlan(UUID planId) {
        return !isPlanInUse(planId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isPlanInUse(UUID planId) {
        return subscriptionRepository.countByPlan(planId) > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public long getActiveSubscriptionsCount(UUID planId) {
        return subscriptionRepository.countByPlan(planId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalPlansCount() {
        return planRepository.count();
    }

    private void createDefaultPlan(String name, String displayName, String description,
                                   BigDecimal price, Integer durationDays, Integer maxStaff,
                                   Integer maxMenuItems, Integer maxTables, Boolean isDefault, Boolean isTrial) {
        if (planRepository.existsByNameAndIsDeletedFalse(name)) {
            return; // Plan already exists
        }

        SubscriptionPlan plan = new SubscriptionPlan();
        plan.setName(name);
        plan.setDisplayName(displayName);
        plan.setDescription(description);
        plan.setPrice(price);
        plan.setDurationDays(durationDays);
        plan.setMaxStaff(maxStaff);
        plan.setMaxMenuItems(maxMenuItems);
        plan.setMaxTables(maxTables);
        plan.setIsActive(true);
        plan.setIsDefault(isDefault);
        plan.setIsTrial(isTrial);
        plan.setSortOrder(getSortOrderForPlan(name));

        planRepository.save(plan);
        log.debug("Created default plan: {}", name);
    }

    private Integer getSortOrderForPlan(String planName) {
        return switch (planName) {
            case "FREE" -> 1;
            case "BASIC" -> 2;
            case "PROFESSIONAL" -> 3;
            case "ENTERPRISE" -> 4;
            default -> 99;
        };
    }
}