package com.emenu.features.subscription.service.impl;

import com.emenu.enums.SubscriptionPlan;
import com.emenu.enums.SubscriptionStatus;
import com.emenu.features.auth.models.Business;
import com.emenu.features.auth.repository.BusinessRepository;
import com.emenu.features.messaging.models.Message;
import com.emenu.features.messaging.repository.MessageRepository;
import com.emenu.features.subscription.dto.request.SubscriptionCreateRequest;
import com.emenu.features.subscription.dto.resposne.SubscriptionResponse;
import com.emenu.features.subscription.mapper.SubscriptionMapper;
import com.emenu.features.subscription.models.Subscription;
import com.emenu.features.subscription.repository.SubscriptionRepository;
import com.emenu.features.subscription.service.SubscriptionService;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.utils.pagination.PaginationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final BusinessRepository businessRepository;
    private final MessageRepository messageRepository;
    private final SubscriptionMapper subscriptionMapper;

    @Override
    public SubscriptionResponse createSubscription(UUID businessId, SubscriptionCreateRequest request) {
        Business business = businessRepository.findByIdAndIsDeletedFalse(businessId)
                .orElseThrow(() -> new RuntimeException("Business not found"));

        // Check if business already has a subscription
        subscriptionRepository.findByBusinessIdAndIsDeletedFalse(businessId)
                .ifPresent(existing -> {
                    throw new RuntimeException("Business already has an active subscription");
                });

        Subscription subscription = subscriptionMapper.toEntity(request);
        subscription.setBusinessId(businessId);
        subscription.setStartDate(LocalDateTime.now());
        
        // Set trial period for eligible plans
        if (request.getPlan() == SubscriptionPlan.FREE) {
            subscription.setTrialPeriod(true);
            subscription.setTrialEndDate(LocalDateTime.now().plusDays(request.getPlan().getTrialDays()));
            subscription.setEndDate(subscription.getTrialEndDate());
        } else {
            // Set end date based on billing cycle
            if ("YEARLY".equals(request.getBillingCycle())) {
                subscription.setEndDate(LocalDateTime.now().plusYears(1));
            } else {
                subscription.setEndDate(LocalDateTime.now().plusMonths(1));
            }
        }

        Subscription savedSubscription = subscriptionRepository.save(subscription);
        log.info("Subscription created for business {} with plan {}", businessId, request.getPlan());

        // Send welcome message
        sendSubscriptionWelcomeMessage(business, savedSubscription);

        return mapToResponse(savedSubscription, business);
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionResponse getSubscriptionById(UUID id) {
        Subscription subscription = subscriptionRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        Business business = businessRepository.findByIdAndIsDeletedFalse(subscription.getBusinessId())
                .orElse(null);

        return mapToResponse(subscription, business);
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionResponse getSubscriptionByBusinessId(UUID businessId) {
        Subscription subscription = subscriptionRepository.findByBusinessIdAndIsDeletedFalse(businessId)
                .orElseThrow(() -> new RuntimeException("No subscription found for business"));

        Business business = businessRepository.findByIdAndIsDeletedFalse(businessId)
                .orElse(null);

        return mapToResponse(subscription, business);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<SubscriptionResponse> getAllSubscriptions(int pageNo, int pageSize) {
        int page = pageNo > 0 ? pageNo - 1 : 0;
        Pageable pageable = PaginationUtils.createPageable(page, pageSize, "createdAt", "DESC");

        Page<Subscription> subscriptionPage = subscriptionRepository.findByIsDeletedFalse(pageable);
        List<SubscriptionResponse> content = subscriptionPage.getContent().stream()
                .map(subscription -> {
                    Business business = businessRepository.findByIdAndIsDeletedFalse(subscription.getBusinessId())
                            .orElse(null);
                    return mapToResponse(subscription, business);
                })
                .toList();

        return PaginationResponse.<SubscriptionResponse>builder()
                .content(content)
                .pageNo(subscriptionPage.getNumber() + 1)
                .pageSize(subscriptionPage.getSize())
                .totalElements(subscriptionPage.getTotalElements())
                .totalPages(subscriptionPage.getTotalPages())
                .first(subscriptionPage.isFirst())
                .last(subscriptionPage.isLast())
                .hasNext(subscriptionPage.hasNext())
                .hasPrevious(subscriptionPage.hasPrevious())
                .build();
    }

    @Override
    public SubscriptionResponse updateSubscription(UUID id, SubscriptionCreateRequest request) {
        Subscription subscription = subscriptionRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        // Update subscription details
        subscription.setPlan(request.getPlan());
        subscription.setMonthlyPrice(request.getPlan().getMonthlyPrice());
        subscription.setBillingCycle(request.getBillingCycle());
        subscription.setAutoRenew(request.getAutoRenew());
        subscription.setCustomMaxStaff(request.getCustomMaxStaff());
        subscription.setCustomMaxMenuItems(request.getCustomMaxMenuItems());
        subscription.setCustomMaxTables(request.getCustomMaxTables());

        Subscription updatedSubscription = subscriptionRepository.save(subscription);
        log.info("Subscription updated: {}", id);

        Business business = businessRepository.findByIdAndIsDeletedFalse(subscription.getBusinessId())
                .orElse(null);

        return mapToResponse(updatedSubscription, business);
    }

    @Override
    public void cancelSubscription(UUID id) {
        Subscription subscription = subscriptionRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscription.setAutoRenew(false);
        subscription.setEndDate(LocalDateTime.now());

        subscriptionRepository.save(subscription);
        log.info("Subscription cancelled: {}", id);

        // Send cancellation notification
        sendSubscriptionCancellationMessage(subscription);
    }

    @Override
    public void renewSubscription(UUID id) {
        Subscription subscription = subscriptionRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        // Extend subscription based on billing cycle
        LocalDateTime newEndDate;
        if ("YEARLY".equals(subscription.getBillingCycle())) {
            newEndDate = subscription.getEndDate().plusYears(1);
        } else {
            newEndDate = subscription.getEndDate().plusMonths(1);
        }

        subscription.setEndDate(newEndDate);
        subscription.setStatus(SubscriptionStatus.ACTIVE);

        subscriptionRepository.save(subscription);
        log.info("Subscription renewed: {}", id);

        // Send renewal confirmation
        sendSubscriptionRenewalMessage(subscription);
    }

    @Override
    public void updateStaffUsage(UUID businessId, int count) {
        subscriptionRepository.findByBusinessIdAndIsDeletedFalse(businessId)
                .ifPresent(subscription -> {
                    subscription.setCurrentStaffCount(count);
                    subscriptionRepository.save(subscription);
                });
    }

    @Override
    public void updateMenuItemUsage(UUID businessId, int count) {
        subscriptionRepository.findByBusinessIdAndIsDeletedFalse(businessId)
                .ifPresent(subscription -> {
                    subscription.setCurrentMenuItems(count);
                    subscriptionRepository.save(subscription);
                });
    }

    @Override
    public void updateTableUsage(UUID businessId, int count) {
        subscriptionRepository.findByBusinessIdAndIsDeletedFalse(businessId)
                .ifPresent(subscription -> {
                    subscription.setCurrentTables(count);
                    subscriptionRepository.save(subscription);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canAddStaff(UUID businessId) {
        return subscriptionRepository.findByBusinessIdAndIsDeletedFalse(businessId)
                .map(Subscription::canAddStaff)
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canAddMenuItem(UUID businessId) {
        return subscriptionRepository.findByBusinessIdAndIsDeletedFalse(businessId)
                .map(Subscription::canAddMenuItem)
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canAddTable(UUID businessId) {
        return subscriptionRepository.findByBusinessIdAndIsDeletedFalse(businessId)
                .map(Subscription::canAddTable)
                .orElse(false);
    }

    @Override
    @Scheduled(cron = "0 0 2 * * ?") // Run daily at 2 AM
    public void processExpiredSubscriptions() {
        List<Subscription> expiredSubscriptions = subscriptionRepository.findExpiredSubscriptions(LocalDateTime.now());
        
        for (Subscription subscription : expiredSubscriptions) {
            if (subscription.getAutoRenew()) {
                try {
                    renewSubscription(subscription.getId());
                } catch (Exception e) {
                    log.error("Failed to auto-renew subscription {}: {}", subscription.getId(), e.getMessage());
                    subscription.setStatus(SubscriptionStatus.EXPIRED);
                    subscriptionRepository.save(subscription);
                }
            } else {
                subscription.setStatus(SubscriptionStatus.EXPIRED);
                subscriptionRepository.save(subscription);
                sendSubscriptionExpirationMessage(subscription);
            }
        }

        log.info("Processed {} expired subscriptions", expiredSubscriptions.size());
    }

    @Override
    @Scheduled(cron = "0 0 10 * * ?") // Run daily at 10 AM
    public void sendExpirationNotifications() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime in7Days = now.plusDays(7);

        List<Subscription> expiringSubscriptions = subscriptionRepository.findExpiringSubscriptions(now, in7Days);
        
        for (Subscription subscription : expiringSubscriptions) {
            sendSubscriptionExpirationWarningMessage(subscription);
        }

        log.info("Sent expiration notifications for {} subscriptions", expiringSubscriptions.size());
    }

    // Helper methods
    private SubscriptionResponse mapToResponse(Subscription subscription, Business business) {
        SubscriptionResponse response = subscriptionMapper.toResponse(subscription);
        
        if (business != null) {
            response.setBusinessName(business.getName());
        }

        // Calculate days remaining
        if (subscription.getEndDate() != null) {
            long daysRemaining = ChronoUnit.DAYS.between(LocalDateTime.now(), subscription.getEndDate());
            response.setDaysRemaining((int) Math.max(0, daysRemaining));
        }

        return response;
    }

    private void sendSubscriptionWelcomeMessage(Business business, Subscription subscription) {
        try {
            Message message = new Message();
            message.setSenderEmail("system@emenu-platform.com");
            message.setSenderName("E-Menu Platform");
            message.setRecipientEmail(business.getEmail());
            message.setRecipientName(business.getName());
            message.setSubject("Welcome to " + subscription.getPlan().getDisplayName());
            message.setContent(String.format(
                    "Welcome to E-Menu Platform!\n\n" +
                    "Your %s subscription is now active.\n\n" +
                    "Plan Details:\n" +
                    "- Max Staff: %d\n" +
                    "- Max Menu Items: %d\n" +
                    "- Max Tables: %d\n\n" +
                    "Best regards,\nE-Menu Platform Team",
                    subscription.getPlan().getDisplayName(),
                    subscription.getMaxStaff(),
                    subscription.getMaxMenuItems(),
                    subscription.getMaxTables()
            ));
            message.setBusinessId(business.getId());

            messageRepository.save(message);
        } catch (Exception e) {
            log.error("Failed to send subscription welcome message", e);
        }
    }

    private void sendSubscriptionCancellationMessage(Subscription subscription) {
        try {
            Business business = businessRepository.findByIdAndIsDeletedFalse(subscription.getBusinessId()).orElse(null);
            if (business == null) return;

            Message message = new Message();
            message.setSenderEmail("system@emenu-platform.com");
            message.setSenderName("E-Menu Platform");
            message.setRecipientEmail(business.getEmail());
            message.setRecipientName(business.getName());
            message.setSubject("Subscription Cancelled");
            message.setContent("Your subscription has been cancelled. Thank you for using E-Menu Platform.");
            message.setBusinessId(business.getId());

            messageRepository.save(message);
        } catch (Exception e) {
            log.error("Failed to send subscription cancellation message", e);
        }
    }

    private void sendSubscriptionRenewalMessage(Subscription subscription) {
        try {
            Business business = businessRepository.findByIdAndIsDeletedFalse(subscription.getBusinessId()).orElse(null);
            if (business == null) return;

            Message message = new Message();
            message.setSenderEmail("system@emenu-platform.com");
            message.setSenderName("E-Menu Platform");
            message.setRecipientEmail(business.getEmail());
            message.setRecipientName(business.getName());
            message.setSubject("Subscription Renewed");
            message.setContent(String.format(
                    "Your %s subscription has been renewed until %s.",
                    subscription.getPlan().getDisplayName(),
                    subscription.getEndDate().toLocalDate()
            ));
            message.setBusinessId(business.getId());

            messageRepository.save(message);
        } catch (Exception e) {
            log.error("Failed to send subscription renewal message", e);
        }
    }

    private void sendSubscriptionExpirationMessage(Subscription subscription) {
        try {
            Business business = businessRepository.findByIdAndIsDeletedFalse(subscription.getBusinessId()).orElse(null);
            if (business == null) return;

            Message message = new Message();
            message.setSenderEmail("system@emenu-platform.com");
            message.setSenderName("E-Menu Platform");
            message.setRecipientEmail(business.getEmail());
            message.setRecipientName(business.getName());
            message.setSubject("Subscription Expired");
            message.setContent("Your subscription has expired. Please renew to continue using our services.");
            message.setBusinessId(business.getId());

            messageRepository.save(message);
        } catch (Exception e) {
            log.error("Failed to send subscription expiration message", e);
        }
    }

    private void sendSubscriptionExpirationWarningMessage(Subscription subscription) {
        try {
            Business business = businessRepository.findByIdAndIsDeletedFalse(subscription.getBusinessId()).orElse(null);
            if (business == null) return;

            long daysRemaining = ChronoUnit.DAYS.between(LocalDateTime.now(), subscription.getEndDate());

            Message message = new Message();
            message.setSenderEmail("system@emenu-platform.com");
            message.setSenderName("E-Menu Platform");
            message.setRecipientEmail(business.getEmail());
            message.setRecipientName(business.getName());
            message.setSubject("Subscription Expiring Soon");
            message.setContent(String.format(
                    "Your %s subscription will expire in %d days on %s. Please renew to avoid service interruption.",
                    subscription.getPlan().getDisplayName(),
                    daysRemaining,
                    subscription.getEndDate().toLocalDate()
            ));
            message.setBusinessId(business.getId());

            messageRepository.save(message);
        } catch (Exception e) {
            log.error("Failed to send subscription expiration warning message", e);
        }
    }
}