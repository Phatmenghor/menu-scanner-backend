package com.emenu.features.auth.service.impl;

import com.emenu.features.auth.dto.filter.SubscriptionFilterRequest;
import com.emenu.features.auth.dto.request.SubscriptionCancelRequest;
import com.emenu.features.auth.dto.request.SubscriptionCreateRequest;
import com.emenu.features.auth.dto.response.SubscriptionResponse;
import com.emenu.features.auth.dto.update.SubscriptionRenewRequest;
import com.emenu.features.auth.dto.update.SubscriptionUpdateRequest;
import com.emenu.features.auth.mapper.SubscriptionMapper;
import com.emenu.features.auth.models.Business;
import com.emenu.features.auth.models.Subscription;
import com.emenu.features.auth.models.SubscriptionPlan;
import com.emenu.features.auth.models.User;
import com.emenu.features.auth.repository.BusinessRepository;
import com.emenu.features.auth.repository.SubscriptionPlanRepository;
import com.emenu.features.auth.repository.SubscriptionRepository;
import com.emenu.features.auth.service.SubscriptionService;
import com.emenu.features.auth.specification.SubscriptionSpecification;
import com.emenu.security.SecurityUtils;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.pagination.PaginationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository planRepository;
    private final BusinessRepository businessRepository;
    private final SubscriptionMapper subscriptionMapper;
    private final SecurityUtils securityUtils;

    @Override
    public SubscriptionResponse createSubscription(SubscriptionCreateRequest request) {
        log.info("Creating subscription for business: {} with plan: {}", request.getBusinessId(), request.getPlanId());

        // Validate business exists
        Business business = businessRepository.findByIdAndIsDeletedFalse(request.getBusinessId())
                .orElseThrow(() -> new RuntimeException("Business not found"));

        // Validate plan exists
        SubscriptionPlan plan = planRepository.findByIdAndIsDeletedFalse(request.getPlanId())
                .orElseThrow(() -> new RuntimeException("Subscription plan not found"));

        // Check for existing active subscription
        var existingSubscription = subscriptionRepository.findCurrentActiveByBusinessId(
                request.getBusinessId(), LocalDateTime.now());
        if (existingSubscription.isPresent()) {
            throw new RuntimeException("Business already has an active subscription");
        }

        // Create subscription
        Subscription subscription = subscriptionMapper.toEntity(request);
        
        // Use provided startDate or current time if not provided
        LocalDateTime startDate = request.getStartDate() != null ? request.getStartDate() : LocalDateTime.now();
        subscription.setStartDate(startDate);
        subscription.setEndDate(startDate.plusDays(plan.getDurationDays()));

        Subscription savedSubscription = subscriptionRepository.save(subscription);
        log.info("Subscription created successfully: {} starting from {}", savedSubscription.getId(), startDate);

        return subscriptionMapper.toResponse(savedSubscription);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<SubscriptionResponse> getSubscriptions(SubscriptionFilterRequest filter) {
        Specification<Subscription> spec = SubscriptionSpecification.buildSpecification(filter);
        
        int pageNo = filter.getPageNo() != null && filter.getPageNo() > 0 ? filter.getPageNo() - 1 : 0;
        Pageable pageable = PaginationUtils.createPageable(
                pageNo, filter.getPageSize(), filter.getSortBy(), filter.getSortDirection()
        );

        Page<Subscription> subscriptionPage = subscriptionRepository.findAll(spec, pageable);
        return subscriptionMapper.toPaginationResponse(subscriptionPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<SubscriptionResponse> getCurrentUserBusinessSubscriptions(SubscriptionFilterRequest filter) {
        User currentUser = securityUtils.getCurrentUser();
        
        if (currentUser.getBusinessId() == null) {
            throw new RuntimeException("User is not associated with any business");
        }

        filter.setBusinessId(currentUser.getBusinessId());
        return getSubscriptions(filter);
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionResponse getSubscriptionById(UUID id) {
        Subscription subscription = subscriptionRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));
        return subscriptionMapper.toResponse(subscription);
    }

    @Override
    public SubscriptionResponse updateSubscription(UUID id, SubscriptionUpdateRequest request) {
        Subscription subscription = subscriptionRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        subscriptionMapper.updateEntity(request, subscription);
        Subscription updatedSubscription = subscriptionRepository.save(subscription);

        log.info("Subscription updated successfully: {}", id);
        return subscriptionMapper.toResponse(updatedSubscription);
    }

    @Override
    public SubscriptionResponse deleteSubscription(UUID id) {
        Subscription subscription = subscriptionRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        // Get response before deleting
        SubscriptionResponse response = subscriptionMapper.toResponse(subscription);

        subscription.softDelete();
        subscriptionRepository.save(subscription);
        log.info("Subscription deleted successfully: {}", id);

        return response;
    }

    @Override
    public SubscriptionResponse renewSubscription(UUID subscriptionId, SubscriptionRenewRequest request) {
        Subscription oldSubscription = subscriptionRepository.findByIdAndIsDeletedFalse(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        // Get plan (new plan or existing plan)
        SubscriptionPlan plan = oldSubscription.getPlan();
        if (request.getNewPlanId() != null) {
            plan = planRepository.findByIdAndIsDeletedFalse(request.getNewPlanId())
                    .orElseThrow(() -> new RuntimeException("New subscription plan not found"));
        }

        // Create new subscription
        Subscription newSubscription = new Subscription();
        newSubscription.setBusinessId(oldSubscription.getBusinessId());
        newSubscription.setPlanId(plan.getId());
        newSubscription.setStartDate(oldSubscription.getEndDate());
        
        int durationDays = request.getCustomDurationDays() != null ? request.getCustomDurationDays() : plan.getDurationDays();
        newSubscription.setEndDate(oldSubscription.getEndDate().plusDays(durationDays));
        newSubscription.setIsActive(true);
        newSubscription.setAutoRenew(oldSubscription.getAutoRenew());
        newSubscription.setNotes(request.getNotes());

        // Deactivate old subscription
        oldSubscription.setIsActive(false);
        subscriptionRepository.save(oldSubscription);

        // Save new subscription
        Subscription savedNewSubscription = subscriptionRepository.save(newSubscription);
        log.info("Subscription renewed successfully: {} -> {}", subscriptionId, savedNewSubscription.getId());

        return subscriptionMapper.toResponse(savedNewSubscription);
    }

    @Override
    public SubscriptionResponse cancelSubscription(UUID subscriptionId, SubscriptionCancelRequest request) {
        Subscription subscription = subscriptionRepository.findByIdAndIsDeletedFalse(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        // Cancel subscription and clear all dates
        subscription.cancel();
        subscription.setStartDate(null);
        subscription.setEndDate(null);
        
        // Add cancellation notes
        if (request.getNotes() != null) {
            String existingNotes = subscription.getNotes() != null ? subscription.getNotes() + "\n" : "";
            subscription.setNotes(existingNotes + "Cancelled: " + request.getNotes());
        }
        
        if (request.getReason() != null) {
            String existingNotes = subscription.getNotes() != null ? subscription.getNotes() + "\n" : "";
            subscription.setNotes(existingNotes + "Reason: " + request.getReason());
        }

        Subscription savedSubscription = subscriptionRepository.save(subscription);
        log.info("Subscription cancelled successfully: {}", subscriptionId);

        return subscriptionMapper.toResponse(savedSubscription);
    }
}