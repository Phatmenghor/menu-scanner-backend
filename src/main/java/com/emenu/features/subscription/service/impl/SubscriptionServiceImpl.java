package com.emenu.features.subscription.service.impl;

import com.emenu.enums.payment.PaymentType;
import com.emenu.exception.custom.ValidationException;
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

        // ✅ FIXED: Use LocalDate and convert to LocalDateTime properly
        LocalDateTime startDate = request.getStartDate() != null ?
                request.getStartDate().atStartOfDay() : LocalDateTime.now();
        subscription.setStartDate(startDate);
        subscription.setEndDate(startDate.plusDays(plan.getDurationDays()));

        Subscription savedSubscription = subscriptionRepository.save(subscription);

        // ✅ FIXED: Update business subscription status
        business.activateSubscription(startDate, subscription.getEndDate());
        businessRepository.save(business);

        log.info("Subscription created successfully: {} starting from {}", savedSubscription.getId(), startDate);

        // ✅ FIXED: Use the new method to properly load relationships
        return loadSubscriptionWithRelationships(savedSubscription.getId());
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

        int pageNo = filter.getPageNo() != null && filter.getPageNo() > 0 ? filter.getPageNo() - 1 : 0;
        Pageable pageable = PaginationUtils.createPageable(
                pageNo, filter.getPageSize(), filter.getSortBy(), filter.getSortDirection()
        );

        // ✅ FIXED: Use repository method that loads relationships for specific business
        Page<Subscription> subscriptionPage = subscriptionRepository.findByBusinessIdWithRelationships(
                currentUser.getBusinessId(), pageable);
        return subscriptionMapper.toPaginationResponse(subscriptionPage);
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionResponse getSubscriptionById(UUID id) {
        // ✅ FIXED: Use the helper method to properly load relationships
        return loadSubscriptionWithRelationships(id);
    }

    @Override
    public SubscriptionResponse updateSubscription(UUID id, SubscriptionUpdateRequest request) {
        // ✅ FIXED: Load with relationships
        Subscription subscription = subscriptionRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        subscriptionMapper.updateEntity(request, subscription);
        Subscription updatedSubscription = subscriptionRepository.save(subscription);

        log.info("Subscription updated successfully: {}", id);

        // ✅ FIXED: Use the helper method to properly load relationships
        return loadSubscriptionWithRelationships(updatedSubscription.getId());
    }

    @Override
    public SubscriptionResponse deleteSubscription(UUID id) {
        // ✅ FIXED: Load with relationships first to get proper response data
        SubscriptionResponse response = loadSubscriptionWithRelationships(id);

        Subscription subscription = subscriptionRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

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

        // ✅ FIXED: Load existing subscription with relationships first
        Subscription oldSubscription = subscriptionRepository.findByIdWithRelationships(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        log.info("Old subscription loaded - Business: {}, Plan: {}",
                oldSubscription.getBusiness() != null ? oldSubscription.getBusiness().getName() : "NULL",
                oldSubscription.getPlan() != null ? oldSubscription.getPlan().getName() : "NULL");

        // ✅ FIXED: Load business and plan entities explicitly to ensure they exist
        Business business = businessRepository.findByIdAndIsDeletedFalse(oldSubscription.getBusinessId())
                .orElseThrow(() -> new RuntimeException("Business not found"));

        // ✅ FIXED: Get plan (new plan or existing plan) - ensure we have the plan entity
        SubscriptionPlan plan;
        if (request.getNewPlanId() != null) {
            plan = planRepository.findByIdAndIsDeletedFalse(request.getNewPlanId())
                    .orElseThrow(() -> new RuntimeException("New subscription plan not found"));
            log.info("Using new plan: {} - ${}", plan.getName(), plan.getPrice());
        } else {
            plan = planRepository.findByIdAndIsDeletedFalse(oldSubscription.getPlanId())
                    .orElseThrow(() -> new RuntimeException("Current subscription plan not found"));
            log.info("Using existing plan: {} - ${}", plan.getName(), plan.getPrice());
        }

        // ✅ FIXED: Create new subscription with proper entity references
        Subscription newSubscription = new Subscription();
        newSubscription.setBusinessId(business.getId());
        newSubscription.setPlanId(plan.getId());

        // ✅ ENHANCED: Set the actual entity references to ensure proper loading
        newSubscription.setBusiness(business);
        newSubscription.setPlan(plan);

        newSubscription.setStartDate(oldSubscription.getEndDate());

        int durationDays = request.getCustomDurationDays() != null ? request.getCustomDurationDays() : plan.getDurationDays();
        newSubscription.setEndDate(oldSubscription.getEndDate().plusDays(durationDays));
        newSubscription.setIsActive(true);
        newSubscription.setAutoRenew(oldSubscription.getAutoRenew());

        // ✅ FIXED: Deactivate old subscription
        oldSubscription.setIsActive(false);
        subscriptionRepository.save(oldSubscription);

        // ✅ FIXED: Save new subscription
        Subscription savedNewSubscription = subscriptionRepository.save(newSubscription);
        log.info("New subscription saved: {} with business: {}, plan: {}",
                savedNewSubscription.getId(),
                savedNewSubscription.getBusiness() != null ? savedNewSubscription.getBusiness().getName() : "NULL",
                savedNewSubscription.getPlan() != null ? savedNewSubscription.getPlan().getName() : "NULL");

        // ✅ FIXED: Update business subscription status
        business.activateSubscription(savedNewSubscription.getStartDate(), savedNewSubscription.getEndDate());
        businessRepository.save(business);

        // ✅ ENHANCED: Create payment if requested
        PaymentResponse paymentResponse = null;
        if (request.shouldCreatePayment()) {
            log.info("💳 Creating payment for subscription renewal: ${}", request.getPaymentAmount());
            try {
                paymentResponse = createRenewalPayment(request, savedNewSubscription);
                log.info("✅ Renewal payment created: {}", paymentResponse.getId());
            } catch (Exception e) {
                log.error("❌ Failed to create renewal payment: {}", e.getMessage(), e);
                // Don't fail the renewal if payment creation fails
            }
        }

        log.info("✅ Subscription renewed successfully: {} -> {}, Payment: {}",
                subscriptionId, savedNewSubscription.getId(), paymentResponse != null ? "✓" : "✗");

        // ✅ FIXED: Reload the subscription with relationships to ensure proper response
        return loadSubscriptionWithRelationships(savedNewSubscription.getId());
    }

    @Override
    public SubscriptionResponse cancelSubscription(UUID subscriptionId, SubscriptionCancelRequest request) {
        log.info("Cancelling subscription: {} with refund amount: {}", subscriptionId, request.getRefundAmount());

        // ✅ FIXED: Load subscription with relationships
        Subscription subscription = subscriptionRepository.findByIdWithRelationships(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        // ✅ ENHANCED: Always clear payments when cancelling
        try {
            clearSubscriptionPayments(subscription);
        } catch (Exception e) {
            log.warn("⚠️ Failed to clear payments during cancellation: {}", e.getMessage());
            // Continue with cancellation even if payment clearing fails
        }

        // ✅ ENHANCED: Create refund if amount provided
        if (request.hasRefundAmount()) {
            try {
                createRefundPayment(request, subscription);
                log.info("✅ Refund payment created for amount: ${}", request.getRefundAmount());
            } catch (Exception e) {
                log.warn("⚠️ Failed to create refund payment: {}", e.getMessage());
                // Continue with cancellation even if refund creation fails
            }
        }

        // ✅ FIXED: Cancel subscription
        subscription.cancel();
        Subscription savedSubscription = subscriptionRepository.save(subscription);

        // ✅ FIXED: Update business subscription status
        if (savedSubscription.getBusinessId() != null) {
            businessRepository.findByIdAndIsDeletedFalse(savedSubscription.getBusinessId())
                    .ifPresent(business -> {
                        business.deactivateSubscription();
                        businessRepository.save(business);
                    });
        }

        log.info("✅ Subscription cancelled successfully: {}", subscriptionId);

        // ✅ FIXED: Reload subscription with relationships after cancellation
        return loadSubscriptionWithRelationships(savedSubscription.getId());
    }

    // ✅ ENHANCED: Helper method to properly load subscription with relationships
    private SubscriptionResponse loadSubscriptionWithRelationships(UUID subscriptionId) {
        log.debug("Loading subscription with relationships: {}", subscriptionId);

        Subscription subscription = subscriptionRepository.findByIdWithRelationships(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        log.debug("Loaded subscription - Business: {}, Plan: {}",
                subscription.getBusiness() != null ? subscription.getBusiness().getName() : "NULL",
                subscription.getPlan() != null ? subscription.getPlan().getName() : "NULL");

        // ✅ ENHANCED: If relationships are still null, try to load them manually
        if (subscription.getBusiness() == null && subscription.getBusinessId() != null) {
            log.warn("Business relationship is null, loading manually for ID: {}", subscription.getBusinessId());
            businessRepository.findByIdAndIsDeletedFalse(subscription.getBusinessId())
                    .ifPresent(subscription::setBusiness);
        }

        if (subscription.getPlan() == null && subscription.getPlanId() != null) {
            log.warn("Plan relationship is null, loading manually for ID: {}", subscription.getPlanId());
            planRepository.findByIdAndIsDeletedFalse(subscription.getPlanId())
                    .ifPresent(subscription::setPlan);
        }

        return subscriptionMapper.toResponse(subscription);
    }

    private PaymentResponse createRenewalPayment(SubscriptionRenewRequest request, Subscription subscription) {
        try {
            log.info("💳 Creating renewal payment for subscription: {}", subscription.getId());

            PaymentCreateRequest paymentRequest = new PaymentCreateRequest();
            paymentRequest.setImageUrl(request.getPaymentImageUrl());
            paymentRequest.setSubscriptionId(subscription.getId());
            paymentRequest.setAmount(request.getPaymentAmount());
            paymentRequest.setPaymentMethod(request.getPaymentMethod());
            paymentRequest.setStatus(request.getPaymentStatus());
            paymentRequest.setPaymentType(PaymentType.SUBSCRIPTION);

            // ✅ FIXED: Don't set reference number - let PaymentService handle it (no uniqueness check now)
            if (request.getPaymentReferenceNumber() != null &&
                    !request.getPaymentReferenceNumber().trim().isEmpty()) {
                paymentRequest.setReferenceNumber(request.getPaymentReferenceNumber().trim());
            }

            String notes = "Subscription renewal payment";
            if (request.getPaymentNotes() != null && !request.getPaymentNotes().trim().isEmpty()) {
                notes += " - " + request.getPaymentNotes();
            }
            paymentRequest.setNotes(notes);

            PaymentResponse paymentResponse = paymentService.createPayment(paymentRequest);
            log.info("✅ Renewal payment created: {} with reference: {}",
                    paymentResponse.getId(), paymentResponse.getReferenceNumber());

            return paymentResponse;

        } catch (ValidationException e) {
            log.error("❌ Validation error creating renewal payment: {}", e.getMessage());
            throw e; // Re-throw validation exceptions as-is
        } catch (Exception e) {
            log.error("❌ Failed to create renewal payment for subscription: {}", subscription.getId(), e);
            throw new RuntimeException("Failed to create renewal payment: " + e.getMessage(), e);
        }
    }

    // Clear subscription payments
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
                                "Cancelled due to subscription cancellation at " + LocalDateTime.now());

                        paymentService.updatePayment(payment.getId(), updateRequest);
                        clearedCount++;
                    }
                }
                log.info("Cleared {} payments for subscription: {}", clearedCount, subscription.getId());
            }
        } catch (Exception e) {
            log.error("Failed to clear payments for subscription: {}", subscription.getId(), e);
            throw e; // Re-throw to let caller handle it
        }
    }

    // Create refund payment record
    private void createRefundPayment(SubscriptionCancelRequest request, Subscription subscription) {
        try {
            PaymentCreateRequest refundRequest = new PaymentCreateRequest();
            refundRequest.setSubscriptionId(subscription.getId());
            refundRequest.setAmount(request.getRefundAmount().negate()); // Negative amount for refund
            refundRequest.setPaymentMethod(com.emenu.enums.payment.PaymentMethod.BANK_TRANSFER); // Default for refunds
            refundRequest.setStatus(com.emenu.enums.payment.PaymentStatus.COMPLETED); // Refund is processed
            refundRequest.setNotes("Refund for cancelled subscription at " + LocalDateTime.now() +
                    (request.getRefundNotes() != null ? ": " + request.getRefundNotes() : ""));
            refundRequest.setPaymentType(PaymentType.SUBSCRIPTION);

            PaymentResponse refundResponse = paymentService.createPayment(refundRequest);
            log.info("Refund payment created: {} for amount: ${}", refundResponse.getId(), request.getRefundAmount());

        } catch (Exception e) {
            log.error("Failed to create refund payment for subscription: {}", subscription.getId(), e);
            throw e; // Re-throw to let caller handle it
        }
    }
}