package com.emenu.features.services.service.impl;

import com.emenu.enums.PaymentStatus;
import com.emenu.exception.UserNotFoundException;
import com.emenu.features.services.domain.PaymentRecord;
import com.emenu.features.services.domain.UserSubscription;
import com.emenu.features.services.dto.filter.PaymentFilterRequest;
import com.emenu.features.services.dto.request.CreatePaymentRequest;
import com.emenu.features.services.dto.response.PaymentResponse;
import com.emenu.features.services.dto.update.UpdatePaymentRequest;
import com.emenu.features.services.service.PaymentService;
import com.emenu.security.SecurityUtils;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.utils.pagination.PaginationUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRecordRepository paymentRepository;
    private final UserSubscriptionRepository subscriptionRepository;
    private final PaymentMapper paymentMapper;
    private final SecurityUtils securityUtils;

    @Override
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        log.info("Creating payment for subscription: {}", request.getSubscriptionId());

        UserSubscription subscription = subscriptionRepository.findByIdAndIsDeletedFalse(request.getSubscriptionId())
                .orElseThrow(() -> new ValidationException("Subscription not found"));

        PaymentRecord payment = new PaymentRecord();
        payment.setUserId(subscription.getUserId());
        payment.setSubscriptionId(request.getSubscriptionId());
        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setDescription(request.getDescription());

        PaymentRecord savedPayment = paymentRepository.save(payment);
        log.info("Payment created: {}", savedPayment.getId());

        return paymentMapper.toResponse(savedPayment);
    }

    @Override
    public PaymentResponse getPayment(UUID id) {
        PaymentRecord payment = paymentRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("Payment not found"));

        return paymentMapper.toResponse(payment);
    }

    @Override
    public PaymentResponse updatePayment(UUID id, UpdatePaymentRequest request) {
        PaymentRecord payment = paymentRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("Payment not found"));

        paymentMapper.updateEntity(request, payment);
        
        if (request.getStatus() == PaymentStatus.COMPLETED && payment.getPaymentDate() == null) {
            payment.setPaymentDate(LocalDateTime.now());
        }

        PaymentRecord savedPayment = paymentRepository.save(payment);
        log.info("Payment updated: {}", savedPayment.getId());

        return paymentMapper.toResponse(savedPayment);
    }

    @Override
    public PaymentResponse refundPayment(UUID id) {
        PaymentRecord payment = paymentRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException("Payment not found"));

        if (!payment.isSuccessful()) {
            throw new ValidationException("Only successful payments can be refunded");
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        PaymentRecord savedPayment = paymentRepository.save(payment);

        log.info("Payment refunded: {}", savedPayment.getId());
        return paymentMapper.toResponse(savedPayment);
    }

    @Override
    public PaginationResponse<PaymentResponse> listPayments(PaymentFilterRequest filter) {
        Specification<PaymentRecord> spec = buildPaymentSpec(filter);
        Pageable pageable = PaginationUtils.createPageable(
                filter.getPageNo(), filter.getPageSize(), filter.getSortBy(), filter.getSortDirection());

        Page<PaymentRecord> paymentPage = paymentRepository.findAll(spec, pageable);
        List<PaymentResponse> content = paymentPage.getContent().stream()
                .map(paymentMapper::toResponse)
                .toList();

        return PaginationResponse.<PaymentResponse>builder()
                .content(content)
                .pageNo(paymentPage.getNumber())
                .pageSize(paymentPage.getSize())
                .totalElements(paymentPage.getTotalElements())
                .totalPages(paymentPage.getTotalPages())
                .last(paymentPage.isLast())
                .first(paymentPage.isFirst())
                .hasNext(paymentPage.hasNext())
                .hasPrevious(paymentPage.hasPrevious())
                .build();
    }

    @Override
    public PaginationResponse<PaymentResponse> getCurrentUserPayments(PaymentFilterRequest filter) {
        UUID currentUserId = securityUtils.getCurrentUserId();
        filter.setSearch(null); // Override search to filter by current user
        
        Specification<PaymentRecord> spec = (root, query, cb) -> {
            var predicates = cb.and();
            predicates = cb.and(predicates, cb.equal(root.get("isDeleted"), false));
            predicates = cb.and(predicates, cb.equal(root.get("userId"), currentUserId));
            return predicates;
        };

        Pageable pageable = PaginationUtils.createPageable(
                filter.getPageNo(), filter.getPageSize(), filter.getSortBy(), filter.getSortDirection());

        Page<PaymentRecord> paymentPage = paymentRepository.findAll(spec, pageable);
        List<PaymentResponse> content = paymentPage.getContent().stream()
                .map(paymentMapper::toResponse)
                .toList();

        return PaginationResponse.<PaymentResponse>builder()
                .content(content)
                .pageNo(paymentPage.getNumber())
                .pageSize(paymentPage.getSize())
                .totalElements(paymentPage.getTotalElements())
                .totalPages(paymentPage.getTotalPages())
                .last(paymentPage.isLast())
                .first(paymentPage.isFirst())
                .hasNext(paymentPage.hasNext())
                .hasPrevious(paymentPage.hasPrevious())
                .build();
    }

    private Specification<PaymentRecord> buildPaymentSpec(PaymentFilterRequest filter) {
        return (root, query, cb) -> {
            var predicates = cb.and();
            predicates = cb.and(predicates, cb.equal(root.get("isDeleted"), false));

            if (filter.getSubscriptionId() != null) {
                predicates = cb.and(predicates, cb.equal(root.get("subscriptionId"), filter.getSubscriptionId()));
            }

            if (filter.getStatus() != null) {
                predicates = cb.and(predicates, cb.equal(root.get("status"), filter.getStatus()));
            }

            if (filter.getPaymentMethod() != null) {
                predicates = cb.and(predicates, cb.equal(root.get("paymentMethod"), filter.getPaymentMethod()));
            }

            return predicates;
        };
    }
}