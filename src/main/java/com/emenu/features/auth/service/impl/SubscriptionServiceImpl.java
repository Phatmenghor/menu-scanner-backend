package com.emenu.features.auth.service.impl;

import com.emenu.features.auth.dto.filter.SubscriptionFilterRequest;
import com.emenu.features.auth.dto.request.SubscriptionCreateRequest;
import com.emenu.features.auth.dto.response.SubscriptionResponse;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        log.info("Creating subscription for business: {}", request.getBusinessId());

        Business business = businessRepository.findByIdAndIsDeletedFalse(request.getBusinessId())
                .orElseThrow(() -> new RuntimeException("Business not found"));

        SubscriptionPlan plan = planRepository.findByIdAndIsDeletedFalse(request.getPlanId())
                .orElseThrow(() -> new RuntimeException("Subscription plan not found"));

        var existingSubscription = subscriptionRepository.findCurrentActiveByBusinessId(
                request.getBusinessId(), LocalDateTime.now());
        if (existingSubscription.isPresent()) {
            throw new RuntimeException("Business already has an active subscription");
        }

        Subscription subscription = subscriptionMapper.toEntity(request);
        
        LocalDateTime now = LocalDateTime.now();
        subscription.setStartDate(now);
        
        int durationDays = subscription.getCustomDurationDays() != null ? 
                          subscription.getCustomDurationDays() : plan.getDurationDays();
        subscription.setEndDate(now.plusDays(durationDays));

        Subscription savedSubscription = subscriptionRepository.save(subscription);
        log.info("Subscription created successfully for business: {}", request.getBusinessId());

        return subscriptionMapper.toResponse(savedSubscription);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<SubscriptionResponse> getSubscriptions(SubscriptionFilterRequest filter) {
        log.debug("Getting subscriptions with filter - Status: {}, BusinessId: {}", filter.getStatus(), filter.getBusinessId());

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
    public SubscriptionResponse getCurrentUserActiveSubscription() {
        User currentUser = securityUtils.getCurrentUser();
        
        if (currentUser.getBusinessId() == null) {
            throw new RuntimeException("User is not associated with any business");
        }

        return getActiveSubscriptionByBusiness(currentUser.getBusinessId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getBusinessSubscriptionHistory(UUID businessId) {
        List<Subscription> subscriptions = subscriptionRepository.findByBusinessIdAndIsDeletedFalse(businessId);
        return subscriptionMapper.toResponseList(subscriptions);
    }

    @Override
    public SubscriptionResponse renewSubscription(UUID subscriptionId, UUID newPlanId, Integer customDurationDays) {
        Subscription subscription = subscriptionRepository.findByIdAndIsDeletedFalse(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        SubscriptionPlan plan = subscription.getPlan();
        
        if (newPlanId != null) {
            plan = planRepository.findByIdAndIsDeletedFalse(newPlanId)
                    .orElseThrow(() -> new RuntimeException("New subscription plan not found"));
        }

        Subscription newSubscription = new Subscription();
        newSubscription.setBusinessId(subscription.getBusinessId());
        newSubscription.setPlanId(plan.getId());
        newSubscription.setStartDate(subscription.getEndDate());
        
        int durationDays = customDurationDays != null ? customDurationDays : plan.getDurationDays();
        newSubscription.setEndDate(subscription.getEndDate().plusDays(durationDays));
        newSubscription.setIsActive(true);
        newSubscription.setAutoRenew(subscription.getAutoRenew());
        newSubscription.setCustomDurationDays(customDurationDays);

        subscription.setIsActive(false);
        subscriptionRepository.save(subscription);

        Subscription savedNewSubscription = subscriptionRepository.save(newSubscription);
        log.info("Subscription renewed successfully: {} -> {}", subscriptionId, savedNewSubscription.getId());

        return subscriptionMapper.toResponse(savedNewSubscription);
    }

    @Override
    public void cancelSubscription(UUID subscriptionId, Boolean immediate) {
        Subscription subscription = subscriptionRepository.findByIdAndIsDeletedFalse(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        if (immediate != null && immediate) {
            subscription.setEndDate(LocalDateTime.now());
        }
        
        subscription.setIsActive(false);
        subscription.setAutoRenew(false);
        subscriptionRepository.save(subscription);

        log.info("Subscription cancelled successfully: {} (immediate: {})", subscriptionId, immediate);
    }

    @Override
    public SubscriptionResponse suspendSubscription(UUID subscriptionId, String reason) {
        Subscription subscription = subscriptionRepository.findByIdAndIsDeletedFalse(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        subscription.setIsActive(false);
        if (reason != null) {
            subscription.setNotes("SUSPENDED: " + reason);
        }
        
        Subscription updatedSubscription = subscriptionRepository.save(subscription);
        log.info("Subscription suspended: {} - Reason: {}", subscriptionId, reason);

        return subscriptionMapper.toResponse(updatedSubscription);
    }

    @Override
    public SubscriptionResponse reactivateSubscription(UUID subscriptionId) {
        Subscription subscription = subscriptionRepository.findByIdAndIsDeletedFalse(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        if (subscription.isExpired()) {
            throw new RuntimeException("Cannot reactivate expired subscription");
        }

        subscription.setIsActive(true);
        Subscription updatedSubscription = subscriptionRepository.save(subscription);
        
        log.info("Subscription reactivated: {}", subscriptionId);
        return subscriptionMapper.toResponse(updatedSubscription);
    }

    @Override
    public SubscriptionResponse extendSubscription(UUID subscriptionId, Integer days, String reason) {
        Subscription subscription = subscriptionRepository.findByIdAndIsDeletedFalse(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        subscription.setEndDate(subscription.getEndDate().plusDays(days));
        if (reason != null) {
            String existingNotes = subscription.getNotes() != null ? subscription.getNotes() : "";
            subscription.setNotes(existingNotes + "\nEXTENDED: " + days + " days - " + reason);
        }

        Subscription updatedSubscription = subscriptionRepository.save(subscription);
        log.info("Subscription extended: {} by {} days", subscriptionId, days);

        return subscriptionMapper.toResponse(updatedSubscription);
    }

    @Override
    public SubscriptionResponse changeSubscriptionPlan(UUID subscriptionId, UUID newPlanId, Boolean immediate) {
        Subscription subscription = subscriptionRepository.findByIdAndIsDeletedFalse(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        SubscriptionPlan newPlan = planRepository.findByIdAndIsDeletedFalse(newPlanId)
                .orElseThrow(() -> new RuntimeException("New subscription plan not found"));

        if (immediate != null && immediate) {
            subscription.setPlanId(newPlanId);
            Subscription updatedSubscription = subscriptionRepository.save(subscription);
            log.info("Subscription plan changed immediately: {} to plan {}", subscriptionId, newPlanId);
            return subscriptionMapper.toResponse(updatedSubscription);
        } else {
            return renewSubscription(subscriptionId, newPlanId, null);
        }
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
    public Object processExpiredSubscriptions() {
        List<Subscription> expired = subscriptionRepository.findExpiredSubscriptions(LocalDateTime.now());
        
        Map<String, Object> result = new HashMap<>();
        List<UUID> processed = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        for (Subscription subscription : expired) {
            try {
                if (subscription.getAutoRenew()) {
                    renewSubscription(subscription.getId(), null, null);
                    log.info("Auto-renewed subscription: {}", subscription.getId());
                } else {
                    subscription.setIsActive(false);
                    subscriptionRepository.save(subscription);
                    log.info("Deactivated expired subscription: {}", subscription.getId());
                }
                processed.add(subscription.getId());
            } catch (Exception e) {
                log.error("Failed to process expired subscription: {}", subscription.getId(), e);
                errors.add("Failed to process subscription " + subscription.getId() + ": " + e.getMessage());
            }
        }
        
        result.put("totalExpired", expired.size());
        result.put("processed", processed.size());
        result.put("errors", errors.size());
        result.put("processedIds", processed);
        result.put("errorMessages", errors);
        
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Object getSubscriptionUsage(UUID subscriptionId) {
        Subscription subscription = subscriptionRepository.findByIdAndIsDeletedFalse(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        Map<String, Object> usage = new HashMap<>();
        usage.put("subscriptionId", subscriptionId);
        usage.put("businessId", subscription.getBusinessId());
        usage.put("planId", subscription.getPlanId());
        usage.put("effectiveMaxStaff", subscription.getEffectiveMaxStaff());
        usage.put("effectiveMaxMenuItems", subscription.getEffectiveMaxMenuItems());
        usage.put("effectiveMaxTables", subscription.getEffectiveMaxTables());
        usage.put("daysRemaining", subscription.getDaysRemaining());
        usage.put("isActive", subscription.getIsActive());
        usage.put("hasCustomLimits", subscription.hasCustomLimits());
        
        return usage;
    }

    @Override
    @Transactional(readOnly = true)
    public Object getBusinessSubscriptionAnalytics(UUID businessId) {
        List<Subscription> subscriptions = subscriptionRepository.findByBusinessIdAndIsDeletedFalse(businessId);
        
        Map<String, Object> analytics = new HashMap<>();
        analytics.put("businessId", businessId);
        analytics.put("totalSubscriptions", subscriptions.size());
        analytics.put("activeSubscriptions", subscriptions.stream().filter(s -> s.getIsActive() && !s.isExpired()).count());
        analytics.put("expiredSubscriptions", subscriptions.stream().filter(Subscription::isExpired).count());
        analytics.put("hasActiveSubscription", hasActiveSubscription(businessId));
        
        return analytics;
    }

    @Override
    public Object bulkOperations(String action, List<UUID> subscriptionIds, String reason) {
        Map<String, Object> result = new HashMap<>();
        List<UUID> successful = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        for (UUID subscriptionId : subscriptionIds) {
            try {
                switch (action.toUpperCase()) {
                    case "CANCEL" -> cancelSubscription(subscriptionId, false);
                    case "SUSPEND" -> suspendSubscription(subscriptionId, reason);
                    case "REACTIVATE" -> reactivateSubscription(subscriptionId);
                    case "EXTEND" -> {
                        Integer days = reason != null ? Integer.parseInt(reason) : 30;
                        extendSubscription(subscriptionId, days, "Bulk extension");
                    }
                    default -> throw new RuntimeException("Unknown action: " + action);
                }
                successful.add(subscriptionId);
            } catch (Exception e) {
                errors.add("Failed to " + action + " subscription " + subscriptionId + ": " + e.getMessage());
            }
        }
        
        result.put("action", action);
        result.put("totalRequested", subscriptionIds.size());
        result.put("successful", successful.size());
        result.put("errors", errors.size());
        result.put("successfulIds", successful);
        result.put("errorMessages", errors);
        
        return result;
    }


    @Override
    public List<SubscriptionResponse> bulkRenewSubscriptions(List<UUID> subscriptionIds) {
        List<SubscriptionResponse> renewed = new ArrayList<>();

        for (UUID subscriptionId : subscriptionIds) {
            try {
                SubscriptionResponse renewedSubscription = renewSubscription(subscriptionId, null, null);
                renewed.add(renewedSubscription);
            } catch (Exception e) {
                log.error("Failed to renew subscription: {}", subscriptionId, e);
            }
        }

        return renewed;
    }

    @Override
    public List<SubscriptionResponse> bulkCancelSubscriptions(List<UUID> subscriptionIds, String reason) {
        List<SubscriptionResponse> cancelled = new ArrayList<>();
        
        for (UUID subscriptionId : subscriptionIds) {
            try {
                cancelSubscription(subscriptionId, false);
                SubscriptionResponse subscription = getSubscriptionById(subscriptionId);
                cancelled.add(subscription);
            } catch (Exception e) {
                log.error("Failed to cancel subscription: {}", subscriptionId, e);
            }
        }
        
        return cancelled;
    }

    @Override
    public List<SubscriptionResponse> bulkSuspendSubscriptions(List<UUID> subscriptionIds, String reason) {
        List<SubscriptionResponse> suspended = new ArrayList<>();
        
        for (UUID subscriptionId : subscriptionIds) {
            try {
                SubscriptionResponse subscription = suspendSubscription(subscriptionId, reason);
                suspended.add(subscription);
            } catch (Exception e) {
                log.error("Failed to suspend subscription: {}", subscriptionId, e);
            }
        }
        
        return suspended;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canAccessSubscription(UUID subscriptionId) {
        try {
            User currentUser = securityUtils.getCurrentUser();
            Subscription subscription = subscriptionRepository.findByIdAndIsDeletedFalse(subscriptionId).orElse(null);
            
            if (subscription == null) return false;
            
            // Platform users can access any subscription
            if (currentUser.isPlatformUser()) return true;
            
            // Business users can only access their own business subscriptions
            return currentUser.getBusinessId() != null && 
                   currentUser.getBusinessId().equals(subscription.getBusinessId());
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canModifySubscription(UUID subscriptionId) {
        try {
            User currentUser = securityUtils.getCurrentUser();
            
            // Only platform admins and business owners can modify subscriptions
            return currentUser.isPlatformUser() || 
                   (currentUser.isBusinessUser() && canAccessSubscription(subscriptionId));
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isValidSubscriptionForBusiness(UUID businessId, UUID planId) {
        boolean businessExists = businessRepository.existsById(businessId);
        boolean planExists = planRepository.findByIdAndIsDeletedFalse(planId).isPresent();
        
        return businessExists && planExists;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasActiveSubscription(UUID businessId) {
        return subscriptionRepository.findCurrentActiveByBusinessId(businessId, LocalDateTime.now()).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canUpgradeToPlan(UUID subscriptionId, UUID newPlanId) {
        // Implementation depends on business rules
        return true; // Simplified
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canDowngradeToPlan(UUID subscriptionId, UUID newPlanId) {
        // Implementation depends on business rules
        return true; // Simplified
    }

    @Override
    public void sendExpiryNotifications() {
        List<Subscription> expiringSoon = subscriptionRepository.findExpiringSubscriptions(
                LocalDateTime.now(), LocalDateTime.now().plusDays(7));
        
        for (Subscription subscription : expiringSoon) {
            log.info("Sending expiry notification for subscription: {}", subscription.getId());
            // Implementation would send actual notifications
        }
    }

    @Override
    public void processAutoRenewals() {
        List<Subscription> autoRenewing = subscriptionRepository.findAutoRenewingSubscriptions(
                LocalDateTime.now(), LocalDateTime.now().plusDays(1));
        
        for (Subscription subscription : autoRenewing) {
            try {
                renewSubscription(subscription.getId(), null, null);
                log.info("Auto-renewed subscription: {}", subscription.getId());
            } catch (Exception e) {
                log.error("Failed to auto-renew subscription: {}", subscription.getId(), e);
            }
        }
    }

    @Override
    public void updateSubscriptionStatuses() {
        List<Subscription> expired = subscriptionRepository.findExpiredSubscriptions(LocalDateTime.now());
        
        for (Subscription subscription : expired) {
            if (subscription.getIsActive()) {
                subscription.setIsActive(false);
                subscriptionRepository.save(subscription);
                log.info("Deactivated expired subscription: {}", subscription.getId());
            }
        }
    }
}