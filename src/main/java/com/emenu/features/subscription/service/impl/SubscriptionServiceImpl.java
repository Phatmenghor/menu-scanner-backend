package com.emenu.features.subscription.service.impl;

import com.emenu.features.auth.models.Business;
import com.emenu.features.auth.repository.BusinessRepository;
import com.emenu.features.payment.models.Payment;
import com.emenu.features.payment.repository.PaymentRepository;
import com.emenu.features.subscription.dto.filter.SubscriptionFilterRequest;
import com.emenu.features.subscription.dto.request.SubscriptionCancelRequest;
import com.emenu.features.subscription.dto.request.SubscriptionCreateRequest;
import com.emenu.features.subscription.dto.request.SubscriptionRenewRequest;
import com.emenu.features.subscription.dto.response.SubscriptionResponse;
import com.emenu.features.subscription.dto.update.SubscriptionUpdateRequest;
import com.emenu.features.subscription.mapper.SubscriptionMapper;
import com.emenu.features.subscription.models.Subscription;
import com.emenu.features.subscription.models.SubscriptionPlan;
import com.emenu.features.subscription.repository.SubscriptionPlanRepository;
import com.emenu.features.subscription.repository.SubscriptionRepository;
import com.emenu.features.subscription.service.SubscriptionService;
import com.emenu.security.SecurityUtils;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.pagination.PaginationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository planRepository;
    private final BusinessRepository businessRepository;
    private final PaymentRepository paymentRepository;
    private final SubscriptionMapper subscriptionMapper;
    private final SecurityUtils securityUtils;

    @Override
    public SubscriptionResponse createSubscription(SubscriptionCreateRequest request) {
        log.info("Creating subscription for business: {} with plan: {}", request.getBusinessId(), request.getPlanId());
        Business business = businessRepository.findById(request.getBusinessId())
                .orElseThrow(() -> new RuntimeException("Business not found: " + request.getBusinessId()));
        SubscriptionPlan plan = planRepository.findByIdAndIsDeletedFalse(request.getPlanId())
                .orElseThrow(() -> new RuntimeException("Subscription plan not found: " + request.getPlanId()));
        Optional<Subscription> existingActive = subscriptionRepository
                .findCurrentActiveByBusinessId(request.getBusinessId(), LocalDateTime.now());
        if (existingActive.isPresent()) {
            throw new RuntimeException("Business already has an active subscription");
        }
        Subscription subscription = subscriptionMapper.toEntity(request);
        subscription.setBusinessId(request.getBusinessId());
        subscription.setPlanId(request.getPlanId());
        LocalDateTime startDate = LocalDateTime.now();
        subscription.setStartDate(startDate);
        subscription.setEndDate(startDate.plusDays(plan.getDurationDays()));
        Subscription savedSubscription = subscriptionRepository.save(subscription);
        business.activateSubscription();
        businessRepository.save(business);
        savedSubscription = subscriptionRepository.findByIdWithRelationships(savedSubscription.getId()).orElse(savedSubscription);
        log.info("Subscription created successfully: {} for business: {}", savedSubscription.getId(), business.getName());
        return subscriptionMapper.toResponse(savedSubscription);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<SubscriptionResponse> getSubscriptions(SubscriptionFilterRequest filter) {
        log.debug("Getting subscriptions - Status: {}, BusinessId: {}", filter.getStatus(), filter.getBusinessId());
        
        int pageNo = filter.getPageNo() != null && filter.getPageNo() > 0 ? filter.getPageNo() - 1 : 0;
        Pageable pageable = PaginationUtils.createPageable(pageNo, filter.getPageSize(), filter.getSortBy(), filter.getSortDirection());
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiryThreshold = "EXPIRING_SOON".equals(filter.getStatus()) 
                ? now.plusDays(filter.getExpiringSoonDays()) 
                : null;
        
        // ONE QUERY CALL - handles all cases!
        Page<Subscription> subscriptionPage = subscriptionRepository.findWithFilters(
                filter.getBusinessId(),
                filter.getPlanId(),
                filter.getAutoRenew(),
                filter.getStartDate(),
                filter.getToDate(),
                filter.getStatus(),
                now,
                expiryThreshold,
                filter.getSearch(),
                pageable
        );
        
        return subscriptionMapper.toPaginationResponse(subscriptionPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<SubscriptionResponse> getCurrentUserBusinessSubscriptions(SubscriptionFilterRequest filter) {
        UUID currentUserId = securityUtils.getCurrentUserId();
        log.debug("Getting subscriptions for current user: {}", currentUserId);
        Business business = businessRepository.findByOwnerIdAndIsDeletedFalse(currentUserId)
                .orElseThrow(() -> new RuntimeException("No business found for current user"));
        filter.setBusinessId(business.getId());
        return getSubscriptions(filter);
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionResponse getSubscriptionById(UUID subscriptionId) {
        log.debug("Getting subscription by ID: {}", subscriptionId);
        Subscription subscription = subscriptionRepository.findByIdAndIsDeletedFalse(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found: " + subscriptionId));
        return subscriptionMapper.toResponse(subscription);
    }

    @Override
    public SubscriptionResponse updateSubscription(UUID subscriptionId, SubscriptionUpdateRequest request) {
        log.info("Updating subscription: {}", subscriptionId);
        Subscription subscription = subscriptionRepository.findByIdAndIsDeletedFalse(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found: " + subscriptionId));
        subscriptionMapper.updateEntity(request, subscription);
        Subscription updatedSubscription = subscriptionRepository.save(subscription);
        updateBusinessSubscriptionStatus(updatedSubscription.getBusinessId());
        updatedSubscription = subscriptionRepository.findByIdWithRelationships(updatedSubscription.getId()).orElse(updatedSubscription);
        log.info("Subscription updated successfully: {}", subscriptionId);
        return subscriptionMapper.toResponse(updatedSubscription);
    }

    @Override
    public SubscriptionResponse deleteSubscription(UUID subscriptionId) {
        log.info("Deleting subscription: {}", subscriptionId);
        Subscription subscription = subscriptionRepository.findByIdAndIsDeletedFalse(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found: " + subscriptionId));
        subscription.softDelete();
        Subscription deletedSubscription = subscriptionRepository.save(subscription);
        updateBusinessSubscriptionStatus(deletedSubscription.getBusinessId());
        deletedSubscription = subscriptionRepository.findByIdWithRelationships(deletedSubscription.getId()).orElse(deletedSubscription);
        log.info("Subscription deleted successfully: {}", subscriptionId);
        return subscriptionMapper.toResponse(deletedSubscription);
    }

    @Override
    public SubscriptionResponse renewSubscription(UUID subscriptionId, SubscriptionRenewRequest request) {
        log.info("Renewing subscription: {} with payment creation: {}", subscriptionId, request.shouldCreatePayment());
        Subscription subscription = subscriptionRepository.findByIdAndIsDeletedFalse(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found: " + subscriptionId));
        subscription.renew();
        Subscription renewedSubscription = subscriptionRepository.save(subscription);
        if (request.shouldCreatePayment()) {
            createPaymentForSubscription(renewedSubscription, request);
        }
        updateBusinessSubscriptionStatus(renewedSubscription.getBusinessId());
        renewedSubscription = subscriptionRepository.findByIdWithRelationships(renewedSubscription.getId()).orElse(renewedSubscription);
        log.info("Subscription renewed successfully: {} - New end date: {}", subscriptionId, renewedSubscription.getEndDate());
        return subscriptionMapper.toResponse(renewedSubscription);
    }

    @Override
    public SubscriptionResponse cancelSubscription(UUID subscriptionId, SubscriptionCancelRequest request) {
        log.info("Cancelling subscription: {} with refund amount: {}", subscriptionId, request.getRefundAmount());
        Subscription subscription = subscriptionRepository.findByIdAndIsDeletedFalse(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found: " + subscriptionId));
        subscription.cancel();
        if (subscription.getPayments() != null && !subscription.getPayments().isEmpty()) {
            subscription.getPayments().stream()
                    .filter(payment -> payment.getStatus().isPending())
                    .forEach(payment -> {
                        payment.markAsFailed();
                        payment.setNotes("Cancelled due to subscription cancellation");
                        paymentRepository.save(payment);
                    });
        }
        Subscription cancelledSubscription = subscriptionRepository.save(subscription);
        if (request.hasRefundAmount()) {
            createRefundForSubscription(cancelledSubscription, request);
        }
        updateBusinessSubscriptionStatus(cancelledSubscription.getBusinessId());
        cancelledSubscription = subscriptionRepository.findByIdWithRelationships(cancelledSubscription.getId()).orElse(cancelledSubscription);
        log.info("Subscription cancelled successfully: {}", subscriptionId);
        return subscriptionMapper.toResponse(cancelledSubscription);
    }

    private void createPaymentForSubscription(Subscription subscription, SubscriptionRenewRequest request) {
        log.debug("Creating payment for subscription: {}", subscription.getId());
        Payment payment = new Payment();
        payment.setBusinessId(subscription.getBusinessId());
        payment.setPlanId(subscription.getPlanId());
        payment.setSubscriptionId(subscription.getId());
        payment.setAmount(request.getPaymentAmount());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setPaymentType(com.emenu.enums.payment.PaymentType.SUBSCRIPTION);
        payment.setStatus(com.emenu.enums.payment.PaymentStatus.COMPLETED);
        payment.setNotes("Payment for subscription renewal");
        paymentRepository.save(payment);
        log.info("Payment created for subscription: {} - Amount: ${}", subscription.getId(), payment.getAmount());
    }

    private void createRefundForSubscription(Subscription subscription, SubscriptionCancelRequest request) {
        log.debug("Creating refund for subscription: {}", subscription.getId());
        Payment refund = new Payment();
        refund.setBusinessId(subscription.getBusinessId());
        refund.setPlanId(subscription.getPlanId());
        refund.setSubscriptionId(subscription.getId());
        refund.setAmount(request.getRefundAmount().negate());
        refund.setPaymentMethod(com.emenu.enums.payment.PaymentMethod.OTHER);
        refund.setPaymentType(com.emenu.enums.payment.PaymentType.REFUND);
        refund.setStatus(com.emenu.enums.payment.PaymentStatus.COMPLETED);
        refund.setNotes("Refund for cancelled subscription");
        paymentRepository.save(refund);
        log.info("Refund created for subscription: {} - Amount: ${}", subscription.getId(), refund.getAmount());
    }

    private void updateBusinessSubscriptionStatus(UUID businessId) {
        Business business = businessRepository.findById(businessId).orElse(null);
        if (business == null) return;
        Optional<Subscription> activeSubscription = subscriptionRepository.findCurrentActiveByBusinessId(businessId, LocalDateTime.now());
        if (activeSubscription.isPresent()) {
            business.activateSubscription();
        } else {
            business.deactivateSubscription();
        }
        businessRepository.save(business);
        log.info("Updated business subscription status: {} - Active: {}", businessId, activeSubscription.isPresent());
    }
}