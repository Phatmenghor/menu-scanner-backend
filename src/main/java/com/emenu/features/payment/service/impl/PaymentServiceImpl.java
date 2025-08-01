package com.emenu.features.payment.service.impl;

import com.emenu.enums.payment.PaymentType;
import com.emenu.exception.custom.NotFoundException;
import com.emenu.exception.custom.ValidationException;
import com.emenu.features.auth.models.Business;
import com.emenu.features.payment.dto.filter.PaymentFilterRequest;
import com.emenu.features.payment.dto.request.PaymentCreateRequest;
import com.emenu.features.payment.dto.response.PaymentResponse;
import com.emenu.features.payment.dto.update.PaymentUpdateRequest;
import com.emenu.features.payment.mapper.PaymentMapper;
import com.emenu.features.payment.models.Payment;
import com.emenu.features.subscription.models.Subscription;
import com.emenu.features.subscription.models.SubscriptionPlan;
import com.emenu.features.auth.repository.BusinessRepository;
import com.emenu.features.payment.repository.PaymentRepository;
import com.emenu.features.subscription.repository.SubscriptionPlanRepository;
import com.emenu.features.subscription.repository.SubscriptionRepository;
import com.emenu.features.payment.service.ExchangeRateService;
import com.emenu.features.payment.service.PaymentService;
import com.emenu.features.payment.specification.PaymentSpecification;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.generate.PaymentReferenceGenerator;
import com.emenu.shared.pagination.PaginationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
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
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final BusinessRepository businessRepository;
    private final SubscriptionPlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final ExchangeRateService exchangeRateService;
    private final PaymentMapper paymentMapper;
    private final PaymentReferenceGenerator referenceGenerator;

    @Override
    public PaymentResponse createPayment(PaymentCreateRequest request) {
        log.info("Creating payment - Amount: {}, Type: {}", request.getAmount(), request.getPaymentType());

        if (!request.isValidPaymentRequest()) {
            throw new ValidationException("Invalid payment request: must specify exactly one payment target");
        }

        // ✅ FIXED: Allow duplicate reference numbers - just use provided or generate new
        String finalReferenceNumber = determineReferenceNumber(request);

        try {
            PaymentResponse response = switch (determinePaymentType(request)) {
                case SUBSCRIPTION -> createPaymentForSubscription(request, finalReferenceNumber);
                case BUSINESS_RECORD -> createPaymentForBusinessRecord(request, finalReferenceNumber);
                case USER_PLAN -> throw new ValidationException("USER_PLAN payment type not implemented");
            };

            log.info("✅ Payment created: {} with reference: {}", response.getId(), finalReferenceNumber);
            return response;

        } catch (DataIntegrityViolationException e) {
            // Handle database-level duplicate reference constraint (if it exists)
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("reference")) {
                log.warn("Database constraint violation for reference: {} - continuing anyway", finalReferenceNumber);
                // Don't throw exception - allow duplicate references
            }
            throw e;
        } catch (Exception e) {
            log.error("Failed to create payment with reference {}: {}", finalReferenceNumber, e.getMessage(), e);
            throw new RuntimeException("Failed to create payment: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<PaymentResponse> getAllPayments(PaymentFilterRequest filter) {
        Specification<Payment> spec = PaymentSpecification.buildSpecification(filter);

        int pageNo = filter.getPageNo() != null && filter.getPageNo() > 0 ? filter.getPageNo() - 1 : 0;
        Pageable pageable = PaginationUtils.createPageable(
                pageNo, filter.getPageSize(), filter.getSortBy(), filter.getSortDirection()
        );

        Page<Payment> paymentPage = paymentRepository.findAll(spec, pageable);
        return paymentMapper.toPaginationResponse(paymentPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(UUID id) {
        Payment payment = paymentRepository.findByIdWithRelationships(id)
                .orElseThrow(() -> new NotFoundException("Payment not found"));
        return paymentMapper.toResponse(payment);
    }

    @Override
    public PaymentResponse updatePayment(UUID id, PaymentUpdateRequest request) {
        Payment payment = findPaymentById(id);

        // ✅ FIXED: Remove uniqueness check for reference numbers
        paymentMapper.updateEntity(request, payment);

        // Recalculate KHR amount if amount changed
        if (request.getAmount() != null) {
            Double currentRate = exchangeRateService.getCurrentRateValue();
            payment.calculateAmountKhr(currentRate);
        }

        Payment updatedPayment = paymentRepository.save(payment);
        log.info("Payment updated successfully: {}", id);

        return paymentMapper.toResponse(updatedPayment);
    }

    @Override
    public PaymentResponse deletePayment(UUID id) {
        Payment payment = findPaymentById(id);

        payment.softDelete();
        payment = paymentRepository.save(payment);

        log.info("Payment deleted successfully: {}", id);
        return paymentMapper.toResponse(payment);
    }

    @Override
    public String generateReferenceNumber() {
        return referenceGenerator.generateUniqueReference();
    }

    // ✅ FIXED: Simplified reference number determination - no uniqueness check
    private String determineReferenceNumber(PaymentCreateRequest request) {
        if (request.getReferenceNumber() != null && !request.getReferenceNumber().trim().isEmpty()) {
            // User provided reference - use as-is (no uniqueness check)
            String userReference = request.getReferenceNumber().trim();
            log.debug("Using user-provided reference: {}", userReference);
            return userReference;
        } else {
            // Generate new reference
            String generatedReference = referenceGenerator.generateUniqueReference();
            log.debug("Generated reference: {}", generatedReference);
            return generatedReference;
        }
    }

    private PaymentType determinePaymentType(PaymentCreateRequest request) {
        if (request.hasSubscriptionInfo()) {
            return PaymentType.SUBSCRIPTION;
        } else if (request.hasBusinessInfo()) {
            return PaymentType.BUSINESS_RECORD;
        }
        throw new ValidationException("Cannot determine payment type from request");
    }

    // Payment for subscription (unchanged)
    private PaymentResponse createPaymentForSubscription(PaymentCreateRequest request, String referenceNumber) {
        log.info("Creating subscription payment with reference: {}", referenceNumber);

        Subscription subscription = subscriptionRepository.findByIdAndIsDeletedFalse(request.getSubscriptionId())
                .orElseThrow(() -> new NotFoundException("Subscription not found"));

        businessRepository.findByIdAndIsDeletedFalse(subscription.getBusinessId())
                .orElseThrow(() -> new NotFoundException("Business not found"));

        planRepository.findByIdAndIsDeletedFalse(subscription.getPlanId())
                .orElseThrow(() -> new NotFoundException("Subscription plan not found"));

        Payment payment = createPaymentEntity(request, referenceNumber);
        payment.setBusinessId(subscription.getBusinessId());
        payment.setPlanId(subscription.getPlanId());
        payment.setSubscriptionId(subscription.getId());

        return finalizePayment(payment);
    }

    // Payment for business record (history only)
    private PaymentResponse createPaymentForBusinessRecord(PaymentCreateRequest request, String referenceNumber) {
        log.info("Creating business payment record with reference: {}", referenceNumber);

        Business business = businessRepository.findByIdAndIsDeletedFalse(request.getBusinessId())
                .orElseThrow(() -> new NotFoundException("Business not found"));

        Payment payment = createPaymentEntity(request, referenceNumber);
        payment.setBusinessId(business.getId());

        // Link to active subscription if available
        subscriptionRepository.findCurrentActiveByBusinessId(business.getId(), LocalDateTime.now())
                .ifPresent(subscription -> {
                    payment.setPlanId(subscription.getPlanId());
                    payment.setSubscriptionId(subscription.getId());
                });

        return finalizePayment(payment);
    }

    // Create payment entity from request
    private Payment createPaymentEntity(PaymentCreateRequest request, String referenceNumber) {
        Payment payment = new Payment();
        payment.setImageUrl(request.getImageUrl());
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setStatus(request.getStatus());
        payment.setNotes(request.getNotes());
        payment.setReferenceNumber(referenceNumber);
        return payment;
    }

    // Finalize payment creation
    private PaymentResponse finalizePayment(Payment payment) {
        // Calculate KHR amount
        Double currentRate = exchangeRateService.getCurrentRateValue();
        payment.calculateAmountKhr(currentRate);

        Payment savedPayment = paymentRepository.save(payment);

        // Load with relationships for response
        Payment paymentWithRelations = paymentRepository.findByIdWithRelationships(savedPayment.getId())
                .orElse(savedPayment);

        return paymentMapper.toResponse(paymentWithRelations);
    }

    private Payment findPaymentById(UUID id) {
        return paymentRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Payment not found"));
    }
}