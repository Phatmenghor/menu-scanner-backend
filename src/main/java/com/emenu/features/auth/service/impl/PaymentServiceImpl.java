package com.emenu.features.auth.service.impl;

import com.emenu.enums.PaymentStatus;
import com.emenu.features.auth.dto.filter.PaymentFilterRequest;
import com.emenu.features.auth.dto.request.PaymentCreateRequest;
import com.emenu.features.auth.dto.response.PaymentResponse;
import com.emenu.features.auth.mapper.PaymentMapper; // ✅ Correct import from mapper package
import com.emenu.features.auth.models.Business;
import com.emenu.features.auth.models.Payment;
import com.emenu.features.auth.models.Subscription;
import com.emenu.features.auth.models.SubscriptionPlan;
import com.emenu.features.auth.repository.BusinessRepository;
import com.emenu.features.auth.repository.PaymentRepository;
import com.emenu.features.auth.repository.SubscriptionPlanRepository;
import com.emenu.features.auth.repository.SubscriptionRepository;
import com.emenu.features.auth.service.PaymentService;
import com.emenu.features.auth.specification.PaymentSpecification;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.pagination.PaginationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final BusinessRepository businessRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final PaymentMapper paymentMapper;

    @Override
    public PaymentResponse recordPayment(PaymentCreateRequest request) {
        log.info("Recording payment for business: {}", request.getBusinessId());

        // Validate business exists
        Business business = businessRepository.findByIdAndIsDeletedFalse(request.getBusinessId())
                .orElseThrow(() -> new RuntimeException("Business not found"));

        // Validate plan exists
        SubscriptionPlan plan = subscriptionPlanRepository.findByIdAndIsDeletedFalse(request.getPlanId())
                .orElseThrow(() -> new RuntimeException("Subscription plan not found"));

        // Create payment
        Payment payment = paymentMapper.toEntity(request);

        // Generate reference number if not provided
        if (payment.getReferenceNumber() == null || payment.getReferenceNumber().isEmpty()) {
            payment.setReferenceNumber(generateReferenceNumber());
        }

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Payment recorded successfully: {}", savedPayment.getReferenceNumber());

        // Auto-complete payment for free plans
        if (plan.isFree()) {
            savedPayment.markAsCompleted();
            paymentRepository.save(savedPayment);

            // Create/extend subscription
            createOrExtendSubscription(savedPayment, plan);
        }

        return paymentMapper.toResponse(savedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<PaymentResponse> getPayments(PaymentFilterRequest filter) {
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
        Payment payment = paymentRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        return paymentMapper.toResponse(payment);
    }

    @Override
    public PaymentResponse completePayment(UUID paymentId) {
        Payment payment = paymentRepository.findByIdAndIsDeletedFalse(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (payment.isCompleted()) {
            throw new RuntimeException("Payment is already completed");
        }

        // Get the plan
        SubscriptionPlan plan = subscriptionPlanRepository.findByIdAndIsDeletedFalse(payment.getPlanId())
                .orElseThrow(() -> new RuntimeException("Subscription plan not found"));

        payment.markAsCompleted();
        Payment updatedPayment = paymentRepository.save(payment);

        // Create/extend subscription
        createOrExtendSubscription(updatedPayment, plan);

        log.info("Payment completed successfully: {}", payment.getReferenceNumber());
        return paymentMapper.toResponse(updatedPayment);
    }

    @Override
    public PaymentResponse failPayment(UUID paymentId) {
        Payment payment = paymentRepository.findByIdAndIsDeletedFalse(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        payment.markAsFailed();
        Payment updatedPayment = paymentRepository.save(payment);

        log.info("Payment marked as failed: {}", payment.getReferenceNumber());
        return paymentMapper.toResponse(updatedPayment);
    }

    @Override
    public PaymentResponse refundPayment(UUID paymentId) {
        Payment payment = paymentRepository.findByIdAndIsDeletedFalse(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (!payment.isCompleted()) {
            throw new RuntimeException("Can only refund completed payments");
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        Payment updatedPayment = paymentRepository.save(payment);

        log.info("Payment refunded successfully: {}", payment.getReferenceNumber());
        return paymentMapper.toResponse(updatedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getBusinessPayments(UUID businessId) {
        List<Payment> payments = paymentRepository.findByBusinessIdAndIsDeletedFalse(businessId);
        return paymentMapper.toResponseList(payments);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalPaidAmount(UUID businessId) {
        BigDecimal total = paymentRepository.getTotalPaidAmountByBusiness(businessId);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalRevenue() {
        LocalDateTime startOfYear = LocalDateTime.now().withDayOfYear(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime now = LocalDateTime.now();

        BigDecimal revenue = paymentRepository.getTotalRevenueInPeriod(startOfYear, now);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getMonthlyRevenue() {
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime now = LocalDateTime.now();

        BigDecimal revenue = paymentRepository.getTotalRevenueInPeriod(startOfMonth, now);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public long getPendingPaymentsCount() {
        return paymentRepository.countByStatus(PaymentStatus.PENDING);
    }

    // Private helper methods
    private String generateReferenceNumber() {
        return "PAY-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
    }

    private void createOrExtendSubscription(Payment payment, SubscriptionPlan plan) {
        try {
            // Check for existing active subscription
            var existingSubscription = subscriptionRepository.findCurrentActiveByBusinessId(
                    payment.getBusinessId(), LocalDateTime.now());

            if (existingSubscription.isPresent()) {
                // Extend existing subscription
                Subscription subscription = existingSubscription.get();
                subscription.extendByDays(plan.getDurationDays());
                subscriptionRepository.save(subscription);
                log.info("Extended subscription for business: {}", payment.getBusinessId());
            } else {
                // Create new subscription
                Subscription newSubscription = new Subscription();
                newSubscription.setBusinessId(payment.getBusinessId());
                newSubscription.setPlanId(plan.getId());
                newSubscription.setStartDate(LocalDateTime.now());
                newSubscription.setEndDate(LocalDateTime.now().plusDays(plan.getDurationDays()));
                newSubscription.setIsActive(true);
                newSubscription.setAutoRenew(false);

                Subscription savedSubscription = subscriptionRepository.save(newSubscription);

                // Link payment to subscription
                payment.setSubscriptionId(savedSubscription.getId());
                paymentRepository.save(payment);

                log.info("Created new subscription for business: {}", payment.getBusinessId());
            }
        } catch (Exception e) {
            log.error("Failed to create/extend subscription for payment: {}", payment.getId(), e);
        }
    }
}