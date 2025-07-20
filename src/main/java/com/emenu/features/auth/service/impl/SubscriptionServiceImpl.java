package com.emenu.features.auth.service.impl;

import com.emenu.exception.custom.ValidationException;
import com.emenu.features.auth.dto.filter.SubscriptionFilterRequest;
import com.emenu.features.auth.dto.request.SubscriptionCreateRequest;
import com.emenu.features.auth.dto.response.SubscriptionResponse;
import com.emenu.features.auth.dto.update.SubscriptionUpdateRequest;
import com.emenu.features.auth.mapper.SubscriptionMapper;
import com.emenu.features.auth.models.Business;
import com.emenu.features.auth.models.Subscription;
import com.emenu.features.auth.repository.BusinessRepository;
import com.emenu.features.auth.repository.SubscriptionRepository;
import com.emenu.features.auth.service.SubscriptionService;
import com.emenu.features.auth.specification.SubscriptionSpecification;
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
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final BusinessRepository businessRepository;
    private final SubscriptionMapper subscriptionMapper;

    @Override
    public SubscriptionResponse createSubscription(SubscriptionCreateRequest request) {
        log.info("Creating subscription for business: {}", request.getBusinessId());

        // Validate business exists
        Business business = businessRepository.findByIdAndIsDeletedFalse(request.getBusinessId())
                .orElseThrow(() -> new ValidationException("Business not found"));

        // Check for existing active subscription
        var existingSubscription = subscriptionRepository.findCurrentActiveByBusinessId(
                request.getBusinessId(), LocalDateTime.now());
        if (existingSubscription.isPresent()) {
            throw new ValidationException("Business already has an active subscription");
        }

        // ✅ Mapper handles entity creation
        Subscription subscription = subscriptionMapper.toEntity(request);
        
        // Set subscription dates
        LocalDateTime now = LocalDateTime.now();
        subscription.setStartDate(now);
        subscription.setEndDate(now.plusDays(subscription.getPlan().getDurationDays()));

        Subscription savedSubscription = subscriptionRepository.save(subscription);
        log.info("Subscription created successfully for business: {}", request.getBusinessId());

        return subscriptionMapper.toResponse(savedSubscription);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<SubscriptionResponse> getSubscriptions(SubscriptionFilterRequest filter) {
        // ✅ Specification handles all filtering logic
        Specification<Subscription> spec = SubscriptionSpecification.buildSpecification(filter);
        
        int pageNo = filter.getPageNo() != null && filter.getPageNo() > 0 ? filter.getPageNo() - 1 : 0;
        Pageable pageable = PaginationUtils.createPageable(
                pageNo, filter.getPageSize(), filter.getSortBy(), filter.getSortDirection()
        );

        Page<Subscription> subscriptionPage = subscriptionRepository.findAll(spec, pageable);

        // ✅ Mapper handles pagination response conversion
        return subscriptionMapper.toPaginationResponse(subscriptionPage);
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

        // ✅ Mapper handles entity updates
        subscriptionMapper.updateEntity(request, subscription);
        Subscription updatedSubscription = subscriptionRepository.save(subscription);

        log.info("Subscription updated successfully: {}", id);
        return subscriptionMapper.toResponse(updatedSubscription);
    }

    @Override
    public void deleteSubscription(UUID id) {
        Subscription subscription = subscriptionRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        subscription.softDelete();
        subscriptionRepository.save(subscription);
        log.info("Subscription deleted successfully: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionResponse getActiveSubscriptionByBusiness(UUID businessId) {
        Subscription subscription = subscriptionRepository.findCurrentActiveByBusinessId(businessId, LocalDateTime.now())
                .orElseThrow(() -> new RuntimeException("No active subscription found for business"));

        return subscriptionMapper.toResponse(subscription);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getBusinessSubscriptionHistory(UUID businessId) {
        List<Subscription> subscriptions = subscriptionRepository.findByBusinessIdAndIsDeletedFalse(businessId);
        return subscriptionMapper.toResponseList(subscriptions);
    }

    @Override
    public SubscriptionResponse renewSubscription(UUID subscriptionId) {
        Subscription subscription = subscriptionRepository.findByIdAndIsDeletedFalse(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        // Create new subscription with same plan
        Subscription newSubscription = new Subscription();
        newSubscription.setBusinessId(subscription.getBusinessId());
        newSubscription.setPlan(subscription.getPlan());
        newSubscription.setStartDate(subscription.getEndDate());
        newSubscription.setEndDate(subscription.getEndDate().plusDays(subscription.getPlan().getDurationDays()));
        newSubscription.setIsActive(true);
        newSubscription.setAutoRenew(subscription.getAutoRenew());

        // Deactivate old subscription
        subscription.setIsActive(false);
        subscriptionRepository.save(subscription);

        Subscription savedNewSubscription = subscriptionRepository.save(newSubscription);
        log.info("Subscription renewed successfully: {} -> {}", subscriptionId, savedNewSubscription.getId());

        return subscriptionMapper.toResponse(savedNewSubscription);
    }

    @Override
    public void cancelSubscription(UUID subscriptionId) {
        Subscription subscription = subscriptionRepository.findByIdAndIsDeletedFalse(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        subscription.setIsActive(false);
        subscription.setAutoRenew(false);
        subscriptionRepository.save(subscription);

        log.info("Subscription cancelled successfully: {}", subscriptionId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getExpiringSubscriptions(int days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime futureDate = now.plusDays(days);
        
        List<Subscription> expiring = subscriptionRepository.findExpiringSubscriptions(now, futureDate);
        return subscriptionMapper.toResponseList(expiring);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getExpiredSubscriptions() {
        List<Subscription> expired = subscriptionRepository.findExpiredSubscriptions(LocalDateTime.now());
        return subscriptionMapper.toResponseList(expired);
    }

    @Override
    public void processExpiredSubscriptions() {
        List<Subscription> expired = subscriptionRepository.findExpiredSubscriptions(LocalDateTime.now());
        
        for (Subscription subscription : expired) {
            if (subscription.getAutoRenew()) {
                try {
                    renewSubscription(subscription.getId());
                    log.info("Auto-renewed subscription: {}", subscription.getId());
                } catch (Exception e) {
                    log.error("Failed to auto-renew subscription: {}", subscription.getId(), e);
                    subscription.setIsActive(false);
                    subscriptionRepository.save(subscription);
                }
            } else {
                subscription.setIsActive(false);
                subscriptionRepository.save(subscription);
                log.info("Deactivated expired subscription: {}", subscription.getId());
            }
        }
    }
}