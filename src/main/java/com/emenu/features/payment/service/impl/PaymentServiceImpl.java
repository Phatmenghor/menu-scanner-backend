package com.emenu.features.payment.service.impl;

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
import com.emenu.shared.pagination.PaginationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    @Override
    public PaymentResponse createPayment(PaymentCreateRequest request) {
        log.info("Creating payment for subscription: {} with amount: {}", request.getSubscriptionId(), request.getAmount());

        // ✅ Get subscription with business and plan loaded
        Subscription subscription = subscriptionRepository.findByIdAndIsDeletedFalse(request.getSubscriptionId())
                .orElseThrow(() -> new NotFoundException("Subscription not found"));

        // ✅ Validate business and plan exist (via subscription)
        businessRepository.findByIdAndIsDeletedFalse(subscription.getBusinessId())
                .orElseThrow(() -> new NotFoundException("Business not found"));

        SubscriptionPlan plan = planRepository.findByIdAndIsDeletedFalse(subscription.getPlanId())
                .orElseThrow(() -> new NotFoundException("Subscription plan not found"));

        // Validate reference number uniqueness if provided
        if (request.getReferenceNumber() != null && isReferenceNumberUnique(request.getReferenceNumber())) {
            throw new ValidationException("Reference number already exists: " + request.getReferenceNumber());
        }

        // ✅ Create payment entity with derived values
        Payment payment = new Payment();
        payment.setImageUrl(request.getImageUrl());
        payment.setBusinessId(subscription.getBusinessId()); // From subscription
        payment.setPlanId(subscription.getPlanId());         // From subscription
        payment.setSubscriptionId(subscription.getId());
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setStatus(request.getStatus());
        payment.setNotes(request.getNotes());

        // Calculate KHR amount using current system exchange rate
        Double currentRate = exchangeRateService.getCurrentRateValue();
        payment.calculateAmountKhr(currentRate);

        // Generate reference number if not provided
        if (request.getReferenceNumber() != null) {
            payment.setReferenceNumber(request.getReferenceNumber());
        }

        Payment savedPayment = paymentRepository.save(payment);

        log.info("Payment created successfully: {} for subscription: {}", savedPayment.getId(), subscription.getId());
        return paymentMapper.toResponse(savedPayment);
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
        // ✅ Use repository method that fetches relationships
        Payment payment = paymentRepository.findByIdWithRelationships(id)
                .orElseThrow(() -> new NotFoundException("Payment not found"));
        return paymentMapper.toResponse(payment);
    }

    @Override
    public PaymentResponse updatePayment(UUID id, PaymentUpdateRequest request) {
        Payment payment = findPaymentById(id);

        // Validate reference number uniqueness if being updated
        if (request.getReferenceNumber() != null &&
                !request.getReferenceNumber().equals(payment.getReferenceNumber()) &&
                isReferenceNumberUnique(request.getReferenceNumber())) {
            throw new ValidationException("Reference number already exists: " + request.getReferenceNumber());
        }

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
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.valueOf((int) (Math.random() * 1000));
        return "PAY-" + timestamp + "-" + String.format("%03d", Integer.parseInt(random));
    }

    private boolean isReferenceNumberUnique(String referenceNumber) {
        return paymentRepository.existsByReferenceNumberAndIsDeletedFalse(referenceNumber);
    }

    private Payment findPaymentById(UUID id) {
        return paymentRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Payment not found"));
    }
}