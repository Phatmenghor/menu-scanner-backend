package com.emenu.features.auth.service.impl;

import com.emenu.exception.custom.ValidationException;
import com.emenu.features.auth.dto.request.SubscriptionPlanCreateRequest;
import com.emenu.features.auth.dto.response.SubscriptionPlanResponse;
import com.emenu.features.auth.dto.update.SubscriptionPlanUpdateRequest;
import com.emenu.features.auth.mapper.SubscriptionPlanMapper;
import com.emenu.features.auth.models.SubscriptionPlan;
import com.emenu.features.auth.repository.SubscriptionPlanRepository;
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
    private final SubscriptionPlanMapper planMapper;

    @Override
    public SubscriptionPlanResponse createPlan(SubscriptionPlanCreateRequest request) {
        log.info("Creating subscription plan: {}", request.getName());

        if (planRepository.existsByNameAndIsDeletedFalse(request.getName())) {
            throw new ValidationException("Plan name already exists");
        }

        SubscriptionPlan plan = planMapper.toEntity(request);
        
        // Set sort order if not provided
        if (plan.getSortOrder() == null) {
            plan.setSortOrder(getNextSortOrder());
        }

        SubscriptionPlan savedPlan = planRepository.save(plan);
        log.info("Subscription plan created successfully: {}", savedPlan.getName());

        return planMapper.toResponse(savedPlan);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<SubscriptionPlanResponse> getAllPlans(int pageNo, int pageSize) {
        int page = pageNo > 0 ? pageNo - 1 : 0;
        Pageable pageable = PaginationUtils.createPageable(page, pageSize, "sortOrder", "ASC");
        
        Page<SubscriptionPlan> planPage = planRepository.findByIsDeletedFalse(pageable);
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
    public List<SubscriptionPlanResponse> getAllActivePlans() {
        List<SubscriptionPlan> plans = planRepository.findAllActivePlans();
        return planMapper.toResponseList(plans);
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionPlanResponse getPlanById(UUID id) {
        SubscriptionPlan plan = planRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Subscription plan not found"));
        return planMapper.toResponse(plan);
    }

    @Override
    public SubscriptionPlanResponse updatePlan(UUID id, SubscriptionPlanUpdateRequest request) {
        SubscriptionPlan plan = planRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Subscription plan not found"));

        // Check name uniqueness if changed
        if (!plan.getName().equals(request.getName()) && 
            planRepository.existsByNameAndIsDeletedFalse(request.getName())) {
            throw new ValidationException("Plan name already exists");
        }

        planMapper.updateEntity(request, plan);
        SubscriptionPlan updatedPlan = planRepository.save(plan);

        log.info("Subscription plan updated successfully: {}", updatedPlan.getName());
        return planMapper.toResponse(updatedPlan);
    }

    @Override
    public void deletePlan(UUID id) {
        SubscriptionPlan plan = planRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Subscription plan not found"));

        // Check if plan is in use
        if (plan.getSubscriptions() != null && !plan.getSubscriptions().isEmpty()) {
            throw new ValidationException("Cannot delete plan that has active subscriptions");
        }

        plan.softDelete();
        planRepository.save(plan);
        log.info("Subscription plan deleted successfully: {}", plan.getName());
    }

    @Override
    public void activatePlan(UUID id) {
        SubscriptionPlan plan = planRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Subscription plan not found"));

        plan.setIsActive(true);
        planRepository.save(plan);
        log.info("Subscription plan activated: {}", plan.getName());
    }

    @Override
    public void deactivatePlan(UUID id) {
        SubscriptionPlan plan = planRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Subscription plan not found"));

        plan.setIsActive(false);
        planRepository.save(plan);
        log.info("Subscription plan deactivated: {}", plan.getName());
    }

    @Override
    public SubscriptionPlanResponse setAsDefault(UUID id) {
        SubscriptionPlan plan = planRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Subscription plan not found"));

        // Remove default from other plans
        List<SubscriptionPlan> defaultPlans = planRepository.findByIsDefaultAndIsDeletedFalse(true);
        defaultPlans.forEach(p -> p.setIsDefault(false));
        planRepository.saveAll(defaultPlans);

        // Set this plan as default
        plan.setIsDefault(true);
        SubscriptionPlan savedPlan = planRepository.save(plan);

        log.info("Set subscription plan as default: {}", plan.getName());
        return planMapper.toResponse(savedPlan);
    }

    @Override
    public SubscriptionPlanResponse createCustomPlan(UUID businessId, SubscriptionPlanCreateRequest request) {
        log.info("Creating custom subscription plan for business: {}", businessId);

        SubscriptionPlan plan = planMapper.toEntity(request);
        plan.setIsCustom(true);
        plan.setName(request.getName() + "_CUSTOM_" + businessId.toString().substring(0, 8));

        SubscriptionPlan savedPlan = planRepository.save(plan);
        log.info("Custom subscription plan created: {}", savedPlan.getName());

        return planMapper.toResponse(savedPlan);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionPlanResponse> getCustomPlansForBusiness(UUID businessId) {
        // This would need additional logic to track which custom plans belong to which business
        // For now, return empty list
        return List.of();
    }

    @Override
    public void seedDefaultPlans() {
        log.info("Seeding default subscription plans...");

        if (planRepository.count() > 0) {
            log.info("Plans already exist, skipping seed");
            return;
        }

        // Free Plan
        createDefaultPlan("FREE", "Free Plan", "Perfect for testing", 
                BigDecimal.ZERO, 30, 1, 10, 2, List.of("Basic Menu", "Basic Orders"), 0);

        // Basic Plan
        createDefaultPlan("BASIC", "Basic Plan", "Great for small restaurants", 
                new BigDecimal("29.99"), 30, 3, 50, 10, 
                List.of("Advanced Menu", "Order Management", "Basic Analytics"), 1);

        // Professional Plan
        createDefaultPlan("PROFESSIONAL", "Professional Plan", "Perfect for growing businesses", 
                new BigDecimal("79.99"), 30, 10, 200, 25, 
                List.of("Full Menu Management", "Advanced Analytics", "Custom Branding"), 2);

        // Enterprise Plan
        createDefaultPlan("ENTERPRISE", "Enterprise Plan", "For large operations", 
                new BigDecimal("199.99"), 30, -1, -1, -1, 
                List.of("Everything", "Priority Support", "Custom Integration"), 3);

        log.info("Default subscription plans seeded successfully");
    }

    private void createDefaultPlan(String name, String displayName, String description,
                                   BigDecimal price, int durationDays, int maxStaff, 
                                   int maxMenuItems, int maxTables, List<String> features, int sortOrder) {
        SubscriptionPlan plan = new SubscriptionPlan();
        plan.setName(name);
        plan.setDisplayName(displayName);
        plan.setDescription(description);
        plan.setPrice(price);
        plan.setDurationDays(durationDays);
        plan.setMaxStaff(maxStaff);
        plan.setMaxMenuItems(maxMenuItems);
        plan.setMaxTables(maxTables);
        plan.setFeatureList(features);
        plan.setSortOrder(sortOrder);
        plan.setIsActive(true);
        plan.setIsDefault(name.equals("FREE"));
        plan.setIsCustom(false);
        
        planRepository.save(plan);
    }

    private Integer getNextSortOrder() {
        return (int) (planRepository.count() + 1);
    }
}