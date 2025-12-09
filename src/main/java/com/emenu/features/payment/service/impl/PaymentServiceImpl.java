package com.emenu.features.payment.service.impl;

import com.emenu.enums.payment.PaymentType;
import com.emenu.exception.custom.NotFoundException;
import com.emenu.exception.custom.ValidationException;
import com.emenu.features.auth.models.Business;
import com.emenu.features.auth.repository.BusinessRepository;
import com.emenu.features.payment.dto.filter.PaymentFilterRequest;
import com.emenu.features.payment.dto.request.PaymentCreateRequest;
import com.emenu.features.payment.dto.response.PaymentResponse;
import com.emenu.features.payment.dto.update.PaymentUpdateRequest;
import com.emenu.features.payment.mapper.PaymentMapper;
import com.emenu.features.payment.models.Payment;
import com.emenu.features.payment.repository.PaymentRepository;
import com.emenu.features.payment.service.ExchangeRateService;
import com.emenu.features.payment.service.PaymentService;
import com.emenu.features.subscription.models.Subscription;
import com.emenu.features.subscription.repository.SubscriptionPlanRepository;
import com.emenu.features.subscription.repository.SubscriptionRepository;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.generate.PaymentReferenceGenerator;
import com.emenu.shared.pagination.PaginationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
        log.info("Creating payment: amount={}, type={}", request.getAmount(), request.getPaymentType());

        String referenceNumber = determineReferenceNumber(request);
        PaymentType paymentType = determinePaymentType(request);

        PaymentResponse response = switch (paymentType) {
            case SUBSCRIPTION -> createSubscriptionPayment(request, referenceNumber);
            case BUSINESS_RECORD -> createBusinessPayment(request, referenceNumber);
            case USER_PLAN -> throw new ValidationException("USER_PLAN not implemented");
        };

        log.info("Payment created: id={}, reference={}", response.getId(), referenceNumber);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<PaymentResponse> getAllPayments(PaymentFilterRequest filter) {
        log.info("Fetching payments: businessId={}, status={}", filter.getBusinessId(), filter.getStatuses());

        int pageNo = filter.getPageNo() != null && filter.getPageNo() > 0 ? filter.getPageNo() - 1 : 0;
        Pageable pageable = PaginationUtils.createPageable(
                pageNo, filter.getPageSize(), filter.getSortBy(), filter.getSortDirection()
        );

        Page<Payment> paymentPage = paymentRepository.findAllWithFilters(
                filter.getBusinessId(),
                filter.getPlanId(),
                filter.getPaymentMethods(),
                filter.getStatuses(),
                filter.getCreatedFrom(),
                filter.getCreatedTo(),
                filter.getSearch(),
                pageable
        );

        return paymentMapper.toPaginationResponse(paymentPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(UUID id) {
        log.info("Fetching payment: id={}", id);
        Payment payment = paymentRepository.findByIdWithRelationships(id)
                .orElseThrow(() -> new NotFoundException("Payment not found"));
        return paymentMapper.toResponse(payment);
    }

    @Override
    public PaymentResponse updatePayment(UUID id, PaymentUpdateRequest request) {
        log.info("Updating payment: id={}", id);

        Payment payment = findPaymentById(id);
        paymentMapper.updateEntity(request, payment);

        if (request.getAmount() != null) {
            Double rate = exchangeRateService.getCurrentRateValue();
            payment.calculateAmountKhr(rate);
        }

        Payment updated = paymentRepository.save(payment);
        return paymentMapper.toResponse(updated);
    }

    @Override
    public PaymentResponse deletePayment(UUID id) {
        log.info("Deleting payment: id={}", id);

        Payment payment = findPaymentById(id);
        payment.softDelete();
        payment = paymentRepository.save(payment);

        return paymentMapper.toResponse(payment);
    }

    @Override
    public String generateReferenceNumber() {
        return referenceGenerator.generateUniqueReference();
    }

    private String determineReferenceNumber(PaymentCreateRequest request) {
        if (request.getReferenceNumber() != null && !request.getReferenceNumber().trim().isEmpty()) {
            return request.getReferenceNumber().trim();
        }
        return referenceGenerator.generateUniqueReference();
    }

    private PaymentType determinePaymentType(PaymentCreateRequest request) {
        if (request.hasSubscriptionInfo()) return PaymentType.SUBSCRIPTION;
        if (request.hasBusinessInfo()) return PaymentType.BUSINESS_RECORD;
        throw new ValidationException("Cannot determine payment type");
    }

    private PaymentResponse createSubscriptionPayment(PaymentCreateRequest request, String referenceNumber) {
        log.info("Creating subscription payment: subscriptionId={}", request.getSubscriptionId());

        Subscription subscription = subscriptionRepository.findByIdAndIsDeletedFalse(request.getSubscriptionId())
                .orElseThrow(() -> new NotFoundException("Subscription not found"));

        businessRepository.findByIdAndIsDeletedFalse(subscription.getBusinessId())
                .orElseThrow(() -> new NotFoundException("Business not found"));

        planRepository.findByIdAndIsDeletedFalse(subscription.getPlanId())
                .orElseThrow(() -> new NotFoundException("Plan not found"));

        Payment payment = createPaymentEntity(request, referenceNumber);
        payment.setBusinessId(subscription.getBusinessId());
        payment.setPlanId(subscription.getPlanId());
        payment.setSubscriptionId(subscription.getId());

        return savePayment(payment);
    }

    private PaymentResponse createBusinessPayment(PaymentCreateRequest request, String referenceNumber) {
        log.info("Creating business payment: businessId={}", request.getBusinessId());

        Business business = businessRepository.findByIdAndIsDeletedFalse(request.getBusinessId())
                .orElseThrow(() -> new NotFoundException("Business not found"));

        Payment payment = createPaymentEntity(request, referenceNumber);
        payment.setBusinessId(business.getId());

        subscriptionRepository.findCurrentActiveByBusinessId(business.getId(), LocalDateTime.now())
                .ifPresent(subscription -> {
                    payment.setPlanId(subscription.getPlanId());
                    payment.setSubscriptionId(subscription.getId());
                });

        return savePayment(payment);
    }

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

    private PaymentResponse savePayment(Payment payment) {
        Double rate = exchangeRateService.getCurrentRateValue();
        payment.calculateAmountKhr(rate);

        Payment saved = paymentRepository.save(payment);
        Payment withRelations = paymentRepository.findByIdWithRelationships(saved.getId()).orElse(saved);

        return paymentMapper.toResponse(withRelations);
    }

    private Payment findPaymentById(UUID id) {
        return paymentRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Payment not found"));
    }
}