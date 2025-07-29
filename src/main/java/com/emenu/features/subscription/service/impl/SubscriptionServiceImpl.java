package com.emenu.features.subscription.service.impl;

import com.emenu.enums.payment.PaymentType;
import com.emenu.features.payment.dto.request.PaymentCreateRequest;
import com.emenu.features.payment.dto.response.PaymentResponse;
import com.emenu.features.payment.dto.update.PaymentUpdateRequest;
import com.emenu.features.payment.models.Payment;
import com.emenu.features.payment.service.PaymentService;
import com.emenu.features.subscription.dto.filter.SubscriptionFilterRequest;
import com.emenu.features.subscription.dto.request.SubscriptionCancelRequest;
import com.emenu.features.subscription.dto.request.SubscriptionCreateRequest;
import com.emenu.features.subscription.dto.response.SubscriptionResponse;
import com.emenu.features.subscription.dto.request.SubscriptionRenewRequest;
import com.emenu.features.subscription.dto.update.SubscriptionUpdateRequest;
import com.emenu.features.subscription.mapper.SubscriptionMapper;
import com.emenu.features.auth.models.Business;
import com.emenu.features.subscription.models.Subscription;
import com.emenu.features.subscription.models.SubscriptionPlan;
import com.emenu.features.auth.models.User;
import com.emenu.features.auth.repository.BusinessRepository;
import com.emenu.features.subscription.repository.SubscriptionPlanRepository;
import com.emenu.features.subscription.repository.SubscriptionRepository;
import com.emenu.features.subscription.service.SubscriptionService;
import com.emenu.features.subscription.specification.SubscriptionSpecification;
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
    private final PaymentService paymentService;

    @Override
    public SubscriptionResponse createSubscription(SubscriptionCreateRequest request) {
        log.info("Creating subscription for business: {} with plan: {}", request.getBusinessId(), request.getPlanId());

        // ✅ FIXED: Validate and load business with all details
        Business business = businessRepository.findByIdAndIsDeletedFalse(request.getBusinessId())
                .orElseThrow(() -> new RuntimeException("Business not found"));

        // ✅ FIXED: Validate and load plan with all details
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
        subscription.setBusinessId(request.getBusinessId());
        subscription.setPlanId(request.getPlanId());
        
        // Use provided startDate or current time if not provided
        LocalDateTime startDate = request.getStartDate() != null ? request.getStartDate() : LocalDateTime.now();
        subscription.setStartDate(startDate);
        subscription.setEndDate(startDate.plusDays(plan.getDurationDays()));

        Subscription savedSubscription = subscriptionRepository.save(subscription);

        // ✅ FIXED: Update business subscription status
        business.activateSubscription(startDate, subscription.getEndDate());
        businessRepository.save(business);

        // ✅ FIXED: Load the saved subscription with relationships
        Subscription subscriptionWithRelations = subscriptionRepository.findByIdAndIsDeletedFalse(savedSubscription.getId())
                .orElse(savedSubscription);

        log.info("Subscription created successfully: {} starting from {}", savedSubscription.getId(), startDate);
        return subscriptionMapper.toResponse(subscriptionWithRelations);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<SubscriptionResponse> getSubscriptions(SubscriptionFilterRequest filter) {
        Specification<Subscription> spec = SubscriptionSpecification.buildSpecification(filter);
        
        int pageNo = filter.getPageNo() != null && filter.getPageNo() > 0 ? filter.getPageNo() - 1 : 0;
        Pageable pageable = PaginationUtils.createPageable(
                pageNo, filter.getPageSize(), filter.getSortBy(), filter.getSortDirection()
        );

        // ✅ FIXED: Use repository method that loads relationships
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
        // ✅ FIXED: Use repository method that loads relationships
        Subscription subscription = subscriptionRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));
        return subscriptionMapper.toResponse(subscription);
    }

    @Override
    public SubscriptionResponse updateSubscription(UUID id, SubscriptionUpdateRequest request) {
        // ✅ FIXED: Load with relationships
        Subscription subscription = subscriptionRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        subscriptionMapper.updateEntity(request, subscription);
        Subscription updatedSubscription = subscriptionRepository.save(subscription);

        // ✅ FIXED: Reload with relationships
        Subscription subscriptionWithRelations = subscriptionRepository.findByIdAndIsDeletedFalse(updatedSubscription.getId())
                .orElse(updatedSubscription);

        log.info("Subscription updated successfully: {}", id);
        return subscriptionMapper.toResponse(subscriptionWithRelations);
    }

    @Override
    public SubscriptionResponse deleteSubscription(UUID id) {
        // ✅ FIXED: Load with relationships
        Subscription subscription = subscriptionRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        // Get response before deleting
        SubscriptionResponse response = subscriptionMapper.toResponse(subscription);

        subscription.softDelete();
        subscriptionRepository.save(subscription);

        // ✅ FIXED: Update business subscription status
        if (subscription.getBusinessId() != null) {
            businessRepository.findByIdAndIsDeletedFalse(subscription.getBusinessId())
                    .ifPresent(business -> {
                        business.deactivateSubscription();
                        businessRepository.save(business);
                    });
        }

        log.info("Subscription deleted successfully: {}", id);
        return response;
    }

    @Override
    public SubscriptionResponse renewSubscription(UUID subscriptionId, SubscriptionRenewRequest request) {
        log.info("Renewing subscription: {} with payment creation: {}", subscriptionId, request.shouldCreatePayment());

        // ✅ Load existing subscription with relationships
        Subscription oldSubscription = subscriptionRepository.findByIdAndIsDeletedFalse(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        // ✅ Get plan (new plan or existing plan)
        SubscriptionPlan plan = oldSubscription.getPlan();
        if (request.getNewPlanId() != null) {
            plan = planRepository.findByIdAndIsDeletedFalse(request.getNewPlanId())
                    .orElseThrow(() -> new RuntimeException("New subscription plan not found"));
        }

        // ✅ Create new subscription
        Subscription newSubscription = new Subscription();
        newSubscription.setBusinessId(oldSubscription.getBusinessId());
        newSubscription.setPlanId(plan.getId());
        newSubscription.setStartDate(oldSubscription.getEndDate());

        int durationDays = request.getCustomDurationDays() != null ? request.getCustomDurationDays() : plan.getDurationDays();
        newSubscription.setEndDate(oldSubscription.getEndDate().plusDays(durationDays));
        newSubscription.setIsActive(true);
        newSubscription.setAutoRenew(oldSubscription.getAutoRenew());
        newSubscription.setNotes(request.getNotes());

        // ✅ Deactivate old subscription
        oldSubscription.setIsActive(false);
        subscriptionRepository.save(oldSubscription);

        // ✅ Save new subscription
        Subscription savedNewSubscription = subscriptionRepository.save(newSubscription);

        // ✅ Update business subscription status
        if (savedNewSubscription.getBusinessId() != null) {
            businessRepository.findByIdAndIsDeletedFalse(savedNewSubscription.getBusinessId())
                    .ifPresent(business -> {
                        business.activateSubscription(savedNewSubscription.getStartDate(), savedNewSubscription.getEndDate());
                        businessRepository.save(business);
                    });
        }

        // ✅ NEW: Create payment if requested
        PaymentResponse paymentResponse = null;
        if (request.shouldCreatePayment()) {
            log.info("💳 Creating payment for subscription renewal: ${}", request.getPaymentAmount());
            paymentResponse = createRenewalPayment(request, savedNewSubscription);
            log.info("✅ Renewal payment created: {}", paymentResponse.getId());
        }

        // ✅ Reload with relationships
        Subscription subscriptionWithRelations = subscriptionRepository.findByIdAndIsDeletedFalse(savedNewSubscription.getId())
                .orElse(savedNewSubscription);

        log.info("✅ Subscription renewed successfully: {} -> {}, Payment: {}",
                subscriptionId, savedNewSubscription.getId(), paymentResponse != null ? "✓" : "✗");

        return subscriptionMapper.toResponse(subscriptionWithRelations);
    }

    @Override
    public SubscriptionResponse cancelSubscription(UUID subscriptionId, SubscriptionCancelRequest request) {
        log.info("Cancelling subscription: {} with payment handling - Clear: {}, Refund: {}",
                subscriptionId, request.shouldClearPayments(), request.shouldCreateRefundRecord());

        // ✅ Load subscription with relationships
        Subscription subscription = subscriptionRepository.findByIdAndIsDeletedFalse(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        // ✅ Handle payment clearing/refunds before cancellation
        if (request.shouldClearPayments()) {
            clearSubscriptionPayments(subscription);
        }

        if (request.shouldCreateRefundRecord()) {
            createRefundPayment(request, subscription);
        }

        // ✅ Cancel subscription and clear all dates
        subscription.cancel();
        subscription.setStartDate(null);
        subscription.setEndDate(null);

        // ✅ Add cancellation notes
        addCancellationNotes(subscription, request);

        Subscription savedSubscription = subscriptionRepository.save(subscription);

        // ✅ Update business subscription status
        if (savedSubscription.getBusinessId() != null) {
            businessRepository.findByIdAndIsDeletedFalse(savedSubscription.getBusinessId())
                    .ifPresent(business -> {
                        business.deactivateSubscription();
                        businessRepository.save(business);
                    });
        }

        log.info("✅ Subscription cancelled successfully: {}", subscriptionId);
        return subscriptionMapper.toResponse(savedSubscription);
    }

    private PaymentResponse createRenewalPayment(SubscriptionRenewRequest request, Subscription subscription) {
        try {
            PaymentCreateRequest paymentRequest = new PaymentCreateRequest();
            paymentRequest.setImageUrl(request.getPaymentImageUrl());
            paymentRequest.setSubscriptionId(subscription.getId());
            paymentRequest.setAmount(request.getPaymentAmount());
            paymentRequest.setPaymentMethod(request.getPaymentMethod());
            paymentRequest.setStatus(request.getPaymentStatus());
            paymentRequest.setReferenceNumber(request.getPaymentReferenceNumber());
            paymentRequest.setNotes("Renewal payment: " + (request.getPaymentNotes() != null ? request.getPaymentNotes() : ""));
            paymentRequest.setPaymentType(PaymentType.SUBSCRIPTION);

            return paymentService.createPayment(paymentRequest);

        } catch (Exception e) {
            log.error("Failed to create renewal payment for subscription: {}", subscription.getId(), e);
            throw new RuntimeException("Failed to create renewal payment: " + e.getMessage(), e);
        }
    }

    // ✅ NEW: Clear subscription payments
    private void clearSubscriptionPayments(Subscription subscription) {
        try {
            if (subscription.getPayments() != null && !subscription.getPayments().isEmpty()) {
                int clearedCount = 0;
                for (Payment payment : subscription.getPayments()) {
                    if (!payment.getStatus().equals(com.emenu.enums.payment.PaymentStatus.CANCELLED)) {
                        // Update payment to cancelled status
                        PaymentUpdateRequest updateRequest = new PaymentUpdateRequest();
                        updateRequest.setStatus(com.emenu.enums.payment.PaymentStatus.CANCELLED);
                        updateRequest.setNotes((payment.getNotes() != null ? payment.getNotes() + "\n" : "") +
                                "Cancelled due to subscription cancellation");

                        paymentService.updatePayment(payment.getId(), updateRequest);
                        clearedCount++;
                    }
                }
                log.info("Cleared {} payments for subscription: {}", clearedCount, subscription.getId());
            }
        } catch (Exception e) {
            log.error("Failed to clear payments for subscription: {}", subscription.getId(), e);
            // Don't fail the cancellation if payment clearing fails
        }
    }

    // ✅ NEW: Create refund payment record
    private void createRefundPayment(SubscriptionCancelRequest request, Subscription subscription) {
        try {
            PaymentCreateRequest refundRequest = new PaymentCreateRequest();
            refundRequest.setSubscriptionId(subscription.getId());
            refundRequest.setAmount(request.getRefundAmount().negate()); // Negative amount for refund
            refundRequest.setPaymentMethod(com.emenu.enums.payment.PaymentMethod.BANK_TRANSFER); // Default for refunds
            refundRequest.setStatus(com.emenu.enums.payment.PaymentStatus.COMPLETED); // Refund is processed
            refundRequest.setNotes("Refund for cancelled subscription: " +
                    (request.getRefundNotes() != null ? request.getRefundNotes() : ""));
            refundRequest.setPaymentType(PaymentType.SUBSCRIPTION);

            PaymentResponse refundResponse = paymentService.createPayment(refundRequest);
            log.info("Refund payment created: {} for amount: ${}", refundResponse.getId(), request.getRefundAmount());

        } catch (Exception e) {
            log.error("Failed to create refund payment for subscription: {}", subscription.getId(), e);
            // Don't fail the cancellation if refund creation fails
        }
    }

    // ✅ Helper method: Add cancellation notes
    private void addCancellationNotes(Subscription subscription, SubscriptionCancelRequest request) {
        StringBuilder notes = new StringBuilder();
        if (subscription.getNotes() != null) {
            notes.append(subscription.getNotes()).append("\n");
        }

        notes.append("Cancelled on: ").append(LocalDateTime.now());

        if (request.getReason() != null) {
            notes.append("\nReason: ").append(request.getReason());
        }

        if (request.getNotes() != null) {
            notes.append("\nNotes: ").append(request.getNotes());
        }

        subscription.setNotes(notes.toString());
    }
}