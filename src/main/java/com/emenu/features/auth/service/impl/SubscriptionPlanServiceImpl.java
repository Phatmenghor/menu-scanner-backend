package com.emenu.features.auth.service.impl;

import com.emenu.features.auth.dto.filter.SubscriptionPlanFilterRequest;
import com.emenu.features.auth.dto.request.SubscriptionCreateRequest;
import com.emenu.features.auth.dto.request.SubscriptionPlanCreateRequest;
import com.emenu.features.auth.dto.response.SubscriptionPlanResponse;
import com.emenu.features.auth.dto.update.SubscriptionPlanUpdateRequest;
import com.emenu.features.auth.mapper.SubscriptionPlanMapper;
import com.emenu.features.auth.models.Business;
import com.emenu.features.auth.models.SubscriptionPlan;
import com.emenu.features.auth.repository.BusinessRepository;
import com.emenu.features.auth.repository.SubscriptionPlanRepository;
import com.emenu.features.auth.repository.SubscriptionRepository;
import com.emenu.features.auth.service.SubscriptionPlanService;
import com.emenu.features.auth.service.SubscriptionService;
import com.emenu.features.auth.specification.SubscriptionPlanSpecification;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SubscriptionPlanServiceImpl implements SubscriptionPlanService {

    private final SubscriptionPlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final BusinessRepository businessRepository;
    private final SubscriptionPlanMapper planMapper;
    private final SubscriptionService subscriptionService;

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
    public PaginationResponse<SubscriptionPlanResponse> getAllPlans(SubscriptionPlanFilterRequest filter) {
        log.debug("Getting subscription plans with filter - Status: {}, Search: {}", filter.getStatus(), filter.getSearch());

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

        Business business = businessRepository.findByIdAndIsDeletedFalse(businessId)
                .orElseThrow(() -> new RuntimeException("Business not found"));

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
        Specification<SubscriptionPlan> spec = SubscriptionPlanSpecification.isCustom();
        List<SubscriptionPlan> plans = planRepository.findAll(spec);
        
        return planMapper.toResponseList(plans);
    }

    @Override
    public SubscriptionPlanResponse assignPlanToBusiness(UUID planId, UUID businessId, Boolean autoRenew, Integer customDurationDays) {
        log.info("Assigning plan {} to business: {}", planId, businessId);

        SubscriptionPlan plan = planRepository.findByIdAndIsDeletedFalse(planId)
                .orElseThrow(() -> new RuntimeException("Subscription plan not found"));
        
        Business business = businessRepository.findByIdAndIsDeletedFalse(businessId)
                .orElseThrow(() -> new RuntimeException("Business not found"));

        SubscriptionCreateRequest subscriptionRequest = new SubscriptionCreateRequest();
        subscriptionRequest.setBusinessId(businessId);
        subscriptionRequest.setPlanId(planId);
        subscriptionRequest.setAutoRenew(autoRenew != null ? autoRenew : false);
        subscriptionRequest.setCustomDurationDays(customDurationDays);

        subscriptionService.createSubscription(subscriptionRequest);

        log.info("Plan {} assigned to business {} successfully", planId, businessId);
        return planMapper.toResponse(plan);
    }

    @Override
    public List<SubscriptionPlanResponse> bulkAssignPlan(UUID planId, List<UUID> businessIds, Boolean autoRenew) {
        log.info("Bulk assigning plan {} to {} businesses", planId, businessIds.size());

        List<SubscriptionPlanResponse> results = new ArrayList<>();
        
        for (UUID businessId : businessIds) {
            try {
                SubscriptionPlanResponse result = assignPlanToBusiness(planId, businessId, autoRenew, null);
                results.add(result);
            } catch (Exception e) {
                log.error("Failed to assign plan {} to business {}: {}", planId, businessId, e.getMessage());
            }
        }

        return results;
    }

    @Override
    public void unassignPlanFromBusiness(UUID planId, UUID businessId) {
        log.info("Unassigning plan {} from business: {}", planId, businessId);
        
        var activeSubscription = subscriptionRepository.findCurrentActiveByBusinessId(businessId, LocalDateTime.now());
        if (activeSubscription.isPresent() && activeSubscription.get().getPlanId().equals(planId)) {
            subscriptionService.cancelSubscription(activeSubscription.get().getId(), true);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Object getPlanStatistics(UUID planId) {
        SubscriptionPlan plan = planRepository.findByIdAndIsDeletedFalse(planId)
                .orElseThrow(() -> new RuntimeException("Subscription plan not found"));

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("planId", planId);
        statistics.put("planName", plan.getDisplayName());
        statistics.put("totalSubscriptions", subscriptionRepository.countByPlan(planId));
        statistics.put("activeSubscriptions", subscriptionRepository.countByPlan(planId));
        statistics.put("isActive", plan.getIsActive());
        statistics.put("price", plan.getPrice());
        statistics.put("createdAt", plan.getCreatedAt());

        return statistics;
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

    @Override
    @Transactional(readOnly = true)
    public Object getPlatformStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPlans", getTotalPlansCount());
        stats.put("activePlans", planRepository.findByIsActiveAndIsDeletedFalseOrderBySortOrder(true).size());
        stats.put("customPlans", planRepository.findAll(SubscriptionPlanSpecification.isCustom()).size());
        stats.put("totalActiveSubscriptions", subscriptionRepository.countActiveSubscriptions());
        
        return stats;
    }

    @Override
    public void seedDefaultPlans() {
        log.info("Seeding default subscription plans");

        if (planRepository.count() > 0) {
            log.info("Plans already exist, skipping seed");
            return;
        }

        createDefaultPlan("FREE", "Free Plan", "Get started with basic features",
                BigDecimal.ZERO, 30, 1, 10, 2, true, true);

        createDefaultPlan("BASIC", "Basic Plan", "Perfect for small restaurants",
                new BigDecimal("29.99"), 30, 3, 50, 10, false, false);

        createDefaultPlan("PROFESSIONAL", "Professional Plan", "Advanced features for growing businesses",
                new BigDecimal("79.99"), 30, 10, 200, 25, false, false);

        createDefaultPlan("ENTERPRISE", "Enterprise Plan", "Full-featured solution for large operations",
                new BigDecimal("199.99"), 30, -1, -1, -1, false, false);

        log.info("Default subscription plans seeded successfully");
    }

    @Override
    public void updateDefaultPlans() {
        log.info("Updating default subscription plans");
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
    public boolean canBusinessUsePlan(UUID businessId, UUID planId) {
        boolean businessExists = businessRepository.existsById(businessId);
        boolean planExists = planRepository.findByIdAndIsDeletedFalse(planId).isPresent();
        
        return businessExists && planExists;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionPlanResponse> getRecommendedPlans(UUID businessId) {
        List<SubscriptionPlan> plans = planRepository.findPublicPlans();
        return planMapper.toResponseList(plans);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionPlanResponse> getSimilarPlans(UUID planId) {
        SubscriptionPlan plan = planRepository.findByIdAndIsDeletedFalse(planId)
                .orElseThrow(() -> new RuntimeException("Plan not found"));

        Specification<SubscriptionPlan> spec = SubscriptionPlanSpecification.isPublic();
        List<SubscriptionPlan> similarPlans = planRepository.findAll(spec);
        return planMapper.toResponseList(similarPlans);
    }

    @Override
    @Transactional(readOnly = true)
    public Object comparePlans(List<UUID> planIds) {
        List<SubscriptionPlan> plans = planRepository.findAllById(planIds);
        List<SubscriptionPlanResponse> planResponses = planMapper.toResponseList(plans);
        
        Map<String, Object> comparison = new HashMap<>();
        comparison.put("plans", planResponses);
        comparison.put("comparedAt", LocalDateTime.now());
        comparison.put("totalPlans", planResponses.size());
        
        return comparison;
    }

    private void createDefaultPlan(String name, String displayName, String description,
                                   BigDecimal price, Integer durationDays, Integer maxStaff,
                                   Integer maxMenuItems, Integer maxTables, Boolean isDefault, Boolean isTrial) {
        if (planRepository.existsByNameAndIsDeletedFalse(name)) {
            return;
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