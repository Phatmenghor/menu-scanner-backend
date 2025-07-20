package com.emenu.features.subscription.service.impl;

import com.emenu.enums.PaymentStatus;
import com.emenu.features.subscription.dto.request.PaymentCreateRequest;
import com.emenu.features.subscription.dto.resposne.PaymentResponse;
import com.emenu.features.subscription.mapper.PaymentMapper;
import com.emenu.features.subscription.models.Payment;
import com.emenu.features.subscription.models.Subscription;
import com.emenu.features.subscription.repository.PaymentRepository;
import com.emenu.features.subscription.repository.SubscriptionRepository;
import com.emenu.features.subscription.service.PaymentService;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.utils.pagination.PaginationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentMapper paymentMapper;

    @Override
    public PaymentResponse createPayment(PaymentCreateRequest request) {
        Subscription subscription = subscriptionRepository.findByIdAndIsDeletedFalse(request.getSubscriptionId())
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        Payment payment = paymentMapper.toEntity(request);
        payment.setSubscription(subscription);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setDueDate(LocalDateTime.now().plusDays(30)); // 30 days to pay
        payment.setInvoiceNumber(generateInvoiceNumber());

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Payment created for subscription: {}", request.getSubscriptionId());

        return paymentMapper.toResponse(savedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(UUID id) {
        Payment payment = paymentRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        return paymentMapper.toResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<PaymentResponse> getPaymentsBySubscription(UUID subscriptionId, int pageNo, int pageSize) {
        int page = pageNo > 0 ? pageNo - 1 : 0;
        Pageable pageable = PaginationUtils.createPageable(page, pageSize, "createdAt", "DESC");

        Page<Payment> paymentPage = paymentRepository.findBySubscriptionIdAndIsDeletedFalse(subscriptionId, pageable);
        List<PaymentResponse> content = paymentMapper.toResponseList(paymentPage.getContent());

        return PaginationResponse.<PaymentResponse>builder()
                .content(content)
                .pageNo(paymentPage.getNumber() + 1)
                .pageSize(paymentPage.getSize())
                .totalElements(paymentPage.getTotalElements())
                .totalPages(paymentPage.getTotalPages())
                .first(paymentPage.isFirst())
                .last(paymentPage.isLast())
                .hasNext(paymentPage.hasNext())
                .hasPrevious(paymentPage.hasPrevious())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getOverduePayments() {
        List<Payment> overduePayments = paymentRepository.findOverduePayments(LocalDateTime.now());
        return paymentMapper.toResponseList(overduePayments);
    }

    @Override
    public PaymentResponse processPayment(UUID paymentId, String transactionId) {
        Payment payment = paymentRepository.findByIdAndIsDeletedFalse(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setTransactionId(transactionId);
        payment.setPaidAt(LocalDateTime.now());

        Payment updatedPayment = paymentRepository.save(payment);
        log.info("Payment processed successfully: {}", paymentId);

        return paymentMapper.toResponse(updatedPayment);
    }

    @Override
    public PaymentResponse refundPayment(UUID paymentId, Double amount) {
        Payment payment = paymentRepository.findByIdAndIsDeletedFalse(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (!payment.isSuccessful()) {
            throw new RuntimeException("Cannot refund unsuccessful payment");
        }

        if (amount > payment.getAmount()) {
            throw new RuntimeException("Refund amount cannot exceed payment amount");
        }

        payment.setRefundedAmount(payment.getRefundedAmount() + amount);
        payment.setRefundedAt(LocalDateTime.now());

        if (payment.getRefundedAmount().equals(payment.getAmount())) {
            payment.setStatus(PaymentStatus.REFUNDED);
        } else {
            payment.setStatus(PaymentStatus.PARTIALLY_REFUNDED);
        }

        Payment updatedPayment = paymentRepository.save(payment);
        log.info("Payment refunded: {} - Amount: {}", paymentId, amount);

        return paymentMapper.toResponse(updatedPayment);
    }

    @Override
    public void markPaymentAsFailed(UUID paymentId, String reason) {
        Payment payment = paymentRepository.findByIdAndIsDeletedFalse(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        payment.setStatus(PaymentStatus.FAILED);
        payment.setFailureReason(reason);

        paymentRepository.save(payment);
        log.info("Payment marked as failed: {} - Reason: {}", paymentId, reason);
    }

    private String generateInvoiceNumber() {
        return "INV-" + System.currentTimeMillis();
    }
}