package com.emenu.features.payment.service.impl;

import com.emenu.exception.custom.NotFoundException;
import com.emenu.exception.custom.ValidationException;
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
        log.info("Creating payment for business: {} with amount: {}", request.getBusinessId(), request.getAmount());

        businessRepository.findByIdAndIsDeletedFalse(request.getBusinessId())
                .orElseThrow(() -> new NotFoundException("Business not found"));

        // Validate plan exists
        SubscriptionPlan plan = planRepository.findByIdAndIsDeletedFalse(request.getPlanId())
                .orElseThrow(() -> new NotFoundException("Subscription plan not found"));

        // ✅ ADDED: Validate subscription if provided
        Subscription subscription = null;
        if (request.getSubscriptionId() != null) {
            subscription = subscriptionRepository.findByIdAndIsDeletedFalse(request.getSubscriptionId())
                    .orElseThrow(() -> new NotFoundException("Subscription not found"));

            // Validate that subscription belongs to the same business and plan
            if (!subscription.getBusinessId().equals(request.getBusinessId())) {
                throw new ValidationException("Subscription does not belong to the specified business");
            }
            if (!subscription.getPlanId().equals(request.getPlanId())) {
                throw new ValidationException("Subscription plan does not match the specified plan");
            }
        }

        // Validate reference number uniqueness if provided
        if (request.getReferenceNumber() != null && isReferenceNumberUnique(request.getReferenceNumber())) {
            throw new ValidationException("Reference number already exists: " + request.getReferenceNumber());
        }

        // Create payment entity
        Payment payment = paymentMapper.toEntity(request);

        // ✅ ADDED: Set subscription if provided
        if (subscription != null) {
            payment.setSubscriptionId(subscription.getId());
        }

        // Calculate KHR amount using current system exchange rate
        Double currentRate = exchangeRateService.getCurrentRateValue();
        payment.calculateAmountKhr(currentRate);

        // Generate reference number if not provided
        if (payment.getReferenceNumber() == null || payment.getReferenceNumber().isEmpty()) {
            payment.setReferenceNumber(generateReferenceNumber());
        }

        Payment savedPayment = paymentRepository.save(payment);

        log.info("Payment created successfully: {}", savedPayment.getId());
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
        Payment payment = findPaymentById(id);
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