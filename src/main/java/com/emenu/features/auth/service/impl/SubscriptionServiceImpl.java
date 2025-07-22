// ===== 5. SIMPLIFIED SubscriptionServiceImpl =====
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
        log.info("Creating subscription for business: {} with plan: {}", request.getBusinessId(), request.getPlanId());

        // Validate business exists
        Business business = businessRepository.findByIdAndIsDeletedFalse(request.getBusinessId())
                .orElseThrow(() -> new RuntimeException("Business not found with ID: " + request.getBusinessId()));

        // Validate plan exists
        SubscriptionPlan plan = planRepository.findByIdAndIsDeletedFalse(request.getPlanId())
                .orElseThrow(() -> new RuntimeException("Subscription plan not found with ID: " + request.getPlanId()));

        // Check for existing active subscription (simplified - just one active per business)
        var existingSubscription = subscriptionRepository.findCurrentActiveByBusinessId(
                request.getBusinessId(), LocalDateTime.now());
        if (existingSubscription.isPresent()) {
            throw new RuntimeException("Business already has an active subscription. Cancel existing subscription first.");
        }

        // Create new subscription
        Subscription subscription = subscriptionMapper.toEntity(request);
        
        LocalDateTime now = LocalDateTime.now();
        subscription.setStartDate(now);
        subscription.setEndDate(now.plusDays(plan.getDurationDays()));

        Subscription savedSubscription = subscriptionRepository.save(subscription);
        log.info("Subscription created successfully: {} for business: {}", savedSubscription.getId(), business.getName());

        return subscriptionMapper.toResponse(savedSubscription);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<SubscriptionResponse> getSubscriptions(SubscriptionFilterRequest filter) {
        log.debug("Getting subscriptions with filter - BusinessId: {}, PlanId: {}", filter.getBusinessId(), filter.getPlanId());

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

        // Set business filter for current user's business
        filter.setBusinessId(currentUser.getBusinessId());
        return getSubscriptions(filter);
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionResponse getSubscriptionById(UUID id) {
        log.debug("Getting subscription by ID: {}", id);
        
        Subscription subscription = subscriptionRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Subscription not found with ID: " + id));

        return subscriptionMapper.toResponse(subscription);
    }

    @Override
    public SubscriptionResponse updateSubscription(UUID id, SubscriptionUpdateRequest request) {
        log.info("Updating subscription: {}", id);
        
        Subscription subscription = subscriptionRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Subscription not found with ID: " + id));

        subscriptionMapper.updateEntity(request, subscription);
        Subscription updatedSubscription = subscriptionRepository.save(subscription);

        log.info("Subscription updated successfully: {}", id);
        return subscriptionMapper.toResponse(updatedSubscription);
    }

    @Override
    public void deleteSubscription(UUID id) {
        log.info("Deleting subscription: {}", id);
        
        Subscription subscription = subscriptionRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Subscription not found with ID: " + id));

        subscription.softDelete();
        subscriptionRepository.save(subscription);
        log.info("Subscription deleted successfully: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionResponse getActiveSubscriptionByBusiness(UUID businessId) {
        log.debug("Getting active subscription for business: {}", businessId);
        
        Subscription subscription = subscriptionRepository.findCurrentActiveByBusinessId(businessId, LocalDateTime.now())
                .orElseThrow(() -> new RuntimeException("No active subscription found for business ID: " + businessId));

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
        log.debug("Getting subscription history for business: {}", businessId);
        
        List<Subscription> subscriptions = subscriptionRepository.findByBusinessIdAndIsDeletedFalse(businessId);
        return subscriptionMapper.toResponseList(subscriptions);
    }

    // ✅ SIMPLIFIED: Basic renewal - create new subscription
    @Override
    public SubscriptionResponse renewSubscription(UUID subscriptionId, UUID newPlanId, Integer customDurationDays) {
        log.info("Renewing subscription: {}", subscriptionId);
        
        Subscription oldSubscription = subscriptionRepository.findByIdAndIsDeletedFalse(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found with ID: " + subscriptionId));

        // Get plan (new plan or existing plan)
        SubscriptionPlan plan = oldSubscription.getPlan();
        if (newPlanId != null) {
            plan = planRepository.findByIdAndIsDeletedFalse(newPlanId)
                    .orElseThrow(() -> new RuntimeException("New subscription plan not found with ID: " + newPlanId));
        }

        // Create new subscription starting from old subscription end date
        Subscription newSubscription = new Subscription();
        newSubscription.setBusinessId(oldSubscription.getBusinessId());
        newSubscription.setPlanId(plan.getId());
        newSubscription.setStartDate(oldSubscription.getEndDate());
        
        int durationDays = customDurationDays != null ? customDurationDays : plan.getDurationDays();
        newSubscription.setEndDate(oldSubscription.getEndDate().plusDays(durationDays));
        newSubscription.setIsActive(true);
        newSubscription.setAutoRenew(oldSubscription.getAutoRenew());

        // Deactivate old subscription
        oldSubscription.setIsActive(false);
        subscriptionRepository.save(oldSubscription);

        // Save new subscription
        Subscription savedNewSubscription = subscriptionRepository.save(newSubscription);
        log.info("Subscription renewed successfully: {} -> {}", subscriptionId, savedNewSubscription.getId());

        return subscriptionMapper.toResponse(savedNewSubscription);
    }

    // ✅ SIMPLIFIED: Basic cancellation
    @Override
    public void cancelSubscription(UUID subscriptionId, Boolean immediate) {
        log.info("Cancelling subscription: {} (immediate: {})", subscriptionId, immediate);
        
        Subscription subscription = subscriptionRepository.findByIdAndIsDeletedFalse(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found with ID: " + subscriptionId));

        if (immediate != null && immediate) {
            subscription.setEndDate(LocalDateTime.now());
        }
        
        subscription.cancel(); // Sets isActive = false, autoRenew = false
        subscriptionRepository.save(subscription);

        log.info("Subscription cancelled successfully: {}", subscriptionId);
    }

    // ✅ SIMPLIFIED: Basic operations
    @Override
    public SubscriptionResponse suspendSubscription(UUID subscriptionId, String reason) {
        log.info("Suspending subscription: {} - Reason: {}", subscriptionId, reason);
        
        Subscription subscription = subscriptionRepository.findByIdAndIsDeletedFalse(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found with ID: " + subscriptionId));

        subscription.setIsActive(false);
        if (reason != null && !reason.isEmpty()) {
            subscription.setNotes("SUSPENDED: " + reason);
        }
        
        Subscription updatedSubscription = subscriptionRepository.save(subscription);
        log.info("Subscription suspended: {}", subscriptionId);

        return subscriptionMapper.toResponse(updatedSubscription);
    }

    @Override
    public SubscriptionResponse reactivateSubscription(UUID subscriptionId) {
        log.info("Reactivating subscription: {}", subscriptionId);
        
        Subscription subscription = subscriptionRepository.findByIdAndIsDeletedFalse(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found with ID: " + subscriptionId));

        if (subscription.isExpired()) {
            throw new RuntimeException("Cannot reactivate expired subscription. Please renew instead.");
        }

        subscription.reactivate(); // Sets isActive = true
        Subscription updatedSubscription = subscriptionRepository.save(subscription);
        
        log.info("Subscription reactivated: {}", subscriptionId);
        return subscriptionMapper.toResponse(updatedSubscription);
    }

    @Override
    public SubscriptionResponse extendSubscription(UUID subscriptionId, Integer days, String reason) {
        log.info("Extending subscription: {} by {} days", subscriptionId, days);
        
        Subscription subscription = subscriptionRepository.findByIdAndIsDeletedFalse(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found with ID: " + subscriptionId));

        subscription.extendByDays(days);
        if (reason != null && !reason.isEmpty()) {
            String existingNotes = subscription.getNotes() != null ? subscription.getNotes() : "";
            subscription.setNotes(existingNotes + "\nEXTENDED: " + days + " days - " + reason);
        }

        Subscription updatedSubscription = subscriptionRepository.save(subscription);
        log.info("Subscription extended: {} by {} days", subscriptionId, days);

        return subscriptionMapper.toResponse(updatedSubscription);
    }

    @Override
    public SubscriptionResponse changeSubscriptionPlan(UUID subscriptionId, UUID newPlanId, Boolean immediate) {
        log.info("Changing subscription {} plan to: {} (immediate: {})", subscriptionId, newPlanId, immediate);
        
        Subscription subscription = subscriptionRepository.findByIdAndIsDeletedFalse(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found with ID: " + subscriptionId));

        SubscriptionPlan newPlan = planRepository.findByIdAndIsDeletedFalse(newPlanId)
                .orElseThrow(() -> new RuntimeException("New subscription plan not found with ID: " + newPlanId));

        if (immediate != null && immediate) {
            // Change plan immediately
            subscription.setPlanId(newPlanId);
            Subscription updatedSubscription = subscriptionRepository.save(subscription);
            log.info("Subscription plan changed immediately: {} to plan {}", subscriptionId, newPlanId);
            return subscriptionMapper.toResponse(updatedSubscription);
        } else {
            // Renew with new plan
            return renewSubscription(subscriptionId, newPlanId, null);
        }
    }

    // ✅ SIMPLIFIED: Basic query methods
    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getExpiringSubscriptions(int days) {
        log.debug("Getting subscriptions expiring in {} days", days);
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime futureDate = now.plusDays(days);
        
        List<Subscription> expiring = subscriptionRepository.findExpiringSubscriptions(now, futureDate);
        return subscriptionMapper.toResponseList(expiring);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getExpiredSubscriptions() {
        log.debug("Getting expired subscriptions");
        
        List<Subscription> expired = subscriptionRepository.findExpiredSubscriptions(LocalDateTime.now());
        return subscriptionMapper.toResponseList(expired);
    }

    // ✅ SIMPLIFIED: Basic processing
    @Override
    public Object processExpiredSubscriptions() {
        log.info("Processing expired subscriptions");
        
        List<Subscription> expired = subscriptionRepository.findExpiredSubscriptions(LocalDateTime.now());
        
        Map<String, Object> result = new HashMap<>();
        List<UUID> processed = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        for (Subscription subscription : expired) {
            try {
                if (subscription.getAutoRenew()) {
                    // Auto-renew expired subscription
                    renewSubscription(subscription.getId(), null, null);
                    log.info("Auto-renewed expired subscription: {}", subscription.getId());
                } else {
                    // Deactivate expired subscription
                    subscription.setIsActive(false);
                    subscriptionRepository.save(subscription);
                    log.info("Deactivated expired subscription: {}", subscription.getId());
                }
                processed.add(subscription.getId());
            } catch (Exception e) {
                String errorMsg = "Failed to process subscription " + subscription.getId() + ": " + e.getMessage();
                errors.add(errorMsg);
                log.error(errorMsg, e);
            }
        }
        
        result.put("totalExpired", expired.size());
        result.put("processed", processed.size());
        result.put("errors", errors.size());
        result.put("processedIds", processed);
        result.put("errorMessages", errors);
        
        log.info("Processed {} expired subscriptions - Success: {}, Errors: {}", 
                expired.size(), processed.size(), errors.size());
        
        return result;
    }

    // ✅ SIMPLIFIED: Basic usage info
    @Override
    @Transactional(readOnly = true)
    public Object getSubscriptionUsage(UUID subscriptionId) {
        log.debug("Getting usage for subscription: {}", subscriptionId);
        
        Subscription subscription = subscriptionRepository.findByIdAndIsDeletedFalse(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found with ID: " + subscriptionId));

        Map<String, Object> usage = new HashMap<>();
        usage.put("subscriptionId", subscriptionId);
        usage.put("businessId", subscription.getBusinessId());
        usage.put("planId", subscription.getPlanId());
        usage.put("planName", subscription.getPlan() != null ? subscription.getPlan().getName() : "Unknown");
        usage.put("daysRemaining", subscription.getDaysRemaining());
        usage.put("isActive", subscription.getIsActive());
        usage.put("isExpired", subscription.isExpired());
        usage.put("autoRenew", subscription.getAutoRenew());
        
        return usage;
    }

    @Override
    @Transactional(readOnly = true)
    public Object getBusinessSubscriptionAnalytics(UUID businessId) {
        log.debug("Getting subscription analytics for business: {}", businessId);
        
        List<Subscription> subscriptions = subscriptionRepository.findByBusinessIdAndIsDeletedFalse(businessId);
        
        Map<String, Object> analytics = new HashMap<>();
        analytics.put("businessId", businessId);
        analytics.put("totalSubscriptions", subscriptions.size());
        analytics.put("activeSubscriptions", subscriptions.stream().filter(s -> s.getIsActive() && !s.isExpired()).count());
        analytics.put("expiredSubscriptions", subscriptions.stream().filter(Subscription::isExpired).count());
        analytics.put("hasActiveSubscription", hasActiveSubscription(businessId));
        analytics.put("autoRenewEnabled", subscriptions.stream().anyMatch(Subscription::getAutoRenew));
        
        return analytics;
    }

    // ✅ SIMPLIFIED: Basic bulk operations
    @Override
    public Object bulkOperations(String action, List<UUID> subscriptionIds, String reason) {
        log.info("Performing bulk {} on {} subscriptions", action, subscriptionIds.size());
        
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
                String errorMsg = "Failed to " + action + " subscription " + subscriptionId + ": " + e.getMessage();
                errors.add(errorMsg);
                log.error(errorMsg, e);
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
        log.info("Bulk renewing {} subscriptions", subscriptionIds.size());
        
        List<SubscriptionResponse> renewed = new ArrayList<>();

        for (UUID subscriptionId : subscriptionIds) {
            try {
                SubscriptionResponse renewedSubscription = renewSubscription(subscriptionId, null, null);
                renewed.add(renewedSubscription);
            } catch (Exception e) {
                log.error("Failed to renew subscription: {}", subscriptionId, e);
            }
        }

        log.info("Bulk renewal completed - Success: {}/{}", renewed.size(), subscriptionIds.size());
        return renewed;
    }

    @Override
    public List<SubscriptionResponse> bulkCancelSubscriptions(List<UUID> subscriptionIds, String reason) {
        log.info("Bulk cancelling {} subscriptions", subscriptionIds.size());
        
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
        
        log.info("Bulk cancellation completed - Success: {}/{}", cancelled.size(), subscriptionIds.size());
        return cancelled;
    }

    @Override
    public List<SubscriptionResponse> bulkSuspendSubscriptions(List<UUID> subscriptionIds, String reason) {
        log.info("Bulk suspending {} subscriptions", subscriptionIds.size());
        
        List<SubscriptionResponse> suspended = new ArrayList<>();
        
        for (UUID subscriptionId : subscriptionIds) {
            try {
                SubscriptionResponse subscription = suspendSubscription(subscriptionId, reason);
                suspended.add(subscription);
            } catch (Exception e) {
                log.error("Failed to suspend subscription: {}", subscriptionId, e);
            }
        }
        
        log.info("Bulk suspension completed - Success: {}/{}", suspended.size(), subscriptionIds.size());
        return suspended;
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
        // Simplified: allow all upgrades
        return isValidSubscriptionForBusiness(subscriptionId, newPlanId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canDowngradeToPlan(UUID subscriptionId, UUID newPlanId) {
        // Simplified: allow all downgrades
        return isValidSubscriptionForBusiness(subscriptionId, newPlanId);
    }

    // ✅ SIMPLIFIED: Automated processes (basic implementations)
    @Override
    public void sendExpiryNotifications() {
        log.info("Sending expiry notifications");
        
        List<Subscription> expiringSoon = subscriptionRepository.findExpiringSubscriptions(
                LocalDateTime.now(), LocalDateTime.now().plusDays(7));
        
        for (Subscription subscription : expiringSoon) {
            log.info("Should send expiry notification for subscription: {} (Business: {})", 
                    subscription.getId(), subscription.getBusinessId());
            // TODO: Implement actual notification sending
        }
    }

    @Override
    public void processAutoRenewals() {
        log.info("Processing auto-renewals");
        
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
        log.info("Updating subscription statuses");
        
        List<Subscription> expired = subscriptionRepository.findExpiredSubscriptions(LocalDateTime.now());
        
        for (Subscription subscription : expired) {
            if (subscription.getIsActive()) {
                subscription.setIsActive(false);
                subscriptionRepository.save(subscription);
                log.debug("Deactivated expired subscription: {}", subscription.getId());
            }
        }
    }
}