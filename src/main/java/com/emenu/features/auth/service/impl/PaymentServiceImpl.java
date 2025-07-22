package com.emenu.features.auth.service.impl;

import com.emenu.enums.PaymentStatus;
import com.emenu.exception.custom.NotFoundException;
import com.emenu.exception.custom.ValidationException;
import com.emenu.features.auth.dto.filter.PaymentFilterRequest;
import com.emenu.features.auth.dto.request.PaymentCreateRequest;
import com.emenu.features.auth.dto.request.PaymentProcessRequest;
import com.emenu.features.auth.dto.response.PaymentResponse;
import com.emenu.features.auth.dto.response.PaymentSummaryResponse;
import com.emenu.features.auth.dto.update.PaymentUpdateRequest;
import com.emenu.features.auth.mapper.PaymentMapper;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private final SecurityUtils securityUtils;

    @Override
    public PaymentResponse recordPayment(PaymentCreateRequest request) {
        log.info("Recording payment for business: {} with amount: {}", request.getBusinessId(), request.getAmount());

        // Validate business exists
        Business business = businessRepository.findByIdAndIsDeletedFalse(request.getBusinessId())
                .orElseThrow(() -> new NotFoundException("Business not found"));

        // Validate plan exists
        SubscriptionPlan plan = subscriptionPlanRepository.findByIdAndIsDeletedFalse(request.getPlanId())
                .orElseThrow(() -> new NotFoundException("Subscription plan not found"));

        // Create payment entity
        Payment payment = paymentMapper.toEntity(request);

        // Set exchange rate if not provided (use business default or system default)
        if (request.getExchangeRate() == null) {
            Double exchangeRate = business.getUsdToKhrRate() != null ? business.getUsdToKhrRate() : 4000.0;
            payment.setExchangeRate(exchangeRate);
            payment.calculateAmountKhr(exchangeRate);
        }

        // Generate reference number if not provided
        if (payment.getReferenceNumber() == null || payment.getReferenceNumber().isEmpty()) {
            payment.setReferenceNumber(generateReferenceNumber());
        } else {
            // Validate uniqueness of provided reference number
            if (!isReferenceNumberUnique(payment.getReferenceNumber())) {
                throw new ValidationException("Reference number already exists: " + payment.getReferenceNumber());
            }
        }

        // Set due date if not provided (default: 30 days from creation)
        if (payment.getDueDate() == null) {
            payment.setDueDate(LocalDateTime.now().plusDays(30));
        }

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Payment recorded successfully with ID: {} and reference: {}", 
                savedPayment.getId(), savedPayment.getReferenceNumber());

        // Auto-complete payment for free plans or if requested
        if (plan.isFree() || Boolean.TRUE.equals(request.getAutoComplete())) {
            return completePayment(savedPayment.getId(), new PaymentProcessRequest());
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
        Payment payment = findPaymentById(id);
        return paymentMapper.toResponse(payment);
    }

    @Override
    public PaymentResponse updatePayment(UUID id, PaymentUpdateRequest request) {
        Payment payment = findPaymentById(id);

        // Only allow updates for pending payments
        if (!payment.isPending()) {
            throw new ValidationException("Can only update pending payments");
        }

        paymentMapper.updateEntity(request, payment);

        // Recalculate KHR amount if amount or exchange rate changed
        if (request.getAmount() != null || request.getExchangeRate() != null) {
            Double exchangeRate = payment.getExchangeRate();
            if (request.getExchangeRate() != null) {
                exchangeRate = request.getExchangeRate();
            }
            payment.calculateAmountKhr(exchangeRate);
        }

        Payment updatedPayment = paymentRepository.save(payment);
        log.info("Payment updated successfully: {}", id);

        return paymentMapper.toResponse(updatedPayment);
    }

    @Override
    public PaymentResponse deletePayment(UUID id) {
        Payment payment = findPaymentById(id);

        // Only allow deletion of pending payments
        if (!payment.isPending()) {
            throw new ValidationException("Can only delete pending payments");
        }

        payment.softDelete();
        payment = paymentRepository.save(payment);

        log.info("Payment deleted successfully: {}", id);
        return paymentMapper.toResponse(payment);
    }

    @Override
    public PaymentResponse completePayment(UUID paymentId, PaymentProcessRequest request) {
        Payment payment = findPaymentById(paymentId);

        if (payment.isCompleted()) {
            throw new ValidationException("Payment is already completed");
        }

        UUID currentUserId = securityUtils.getCurrentUserId();
        payment.markAsCompleted(currentUserId);

        // Update additional fields from request
        updatePaymentFromProcessRequest(payment, request);

        Payment updatedPayment = paymentRepository.save(payment);

        // Create or extend subscription
        try {
            extendSubscriptionAfterPayment(paymentId);
        } catch (Exception e) {
            log.error("Failed to extend subscription after payment completion: {}", paymentId, e);
            // Don't fail the payment completion, just log the error
        }

        log.info("Payment completed successfully: {} by user: {}", paymentId, currentUserId);
        return paymentMapper.toResponse(updatedPayment);
    }

    @Override
    public PaymentResponse failPayment(UUID paymentId, PaymentProcessRequest request) {
        Payment payment = findPaymentById(paymentId);

        if (!payment.isPending()) {
            throw new ValidationException("Can only fail pending payments");
        }

        UUID currentUserId = securityUtils.getCurrentUserId();
        payment.markAsFailed(currentUserId, request.getReason());

        updatePaymentFromProcessRequest(payment, request);
        Payment updatedPayment = paymentRepository.save(payment);

        log.info("Payment marked as failed: {} by user: {}", paymentId, currentUserId);
        return paymentMapper.toResponse(updatedPayment);
    }

    @Override
    public PaymentResponse refundPayment(UUID paymentId, PaymentProcessRequest request) {
        Payment payment = findPaymentById(paymentId);

        if (!payment.isCompleted()) {
            throw new ValidationException("Can only refund completed payments");
        }

        UUID currentUserId = securityUtils.getCurrentUserId();
        payment.markAsRefunded(currentUserId, request.getReason());

        updatePaymentFromProcessRequest(payment, request);
        Payment updatedPayment = paymentRepository.save(payment);

        log.info("Payment refunded successfully: {} by user: {}", paymentId, currentUserId);
        return paymentMapper.toResponse(updatedPayment);
    }

    @Override
    public PaymentResponse cancelPayment(UUID paymentId, PaymentProcessRequest request) {
        Payment payment = findPaymentById(paymentId);

        if (!payment.isPending()) {
            throw new ValidationException("Can only cancel pending payments");
        }

        UUID currentUserId = securityUtils.getCurrentUserId();
        payment.setStatus(PaymentStatus.CANCELLED);
        payment.setProcessedBy(currentUserId);
        payment.setProcessedAt(LocalDateTime.now());

        updatePaymentFromProcessRequest(payment, request);
        Payment updatedPayment = paymentRepository.save(payment);

        log.info("Payment cancelled successfully: {} by user: {}", paymentId, currentUserId);
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
    public PaginationResponse<PaymentResponse> getBusinessPaymentsPaginated(UUID businessId, PaymentFilterRequest filter) {
        filter.setBusinessId(businessId);
        return getPayments(filter);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getBusinessTotalPaidAmount(UUID businessId) {
        BigDecimal total = paymentRepository.getTotalPaidAmountByBusiness(businessId);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentSummaryResponse getBusinessPaymentSummary(UUID businessId) {
        // Get counts by status
        long totalPayments = paymentRepository.countByBusinessIdAndStatus(businessId, null);
        long completedPayments = paymentRepository.countByBusinessIdAndStatus(businessId, PaymentStatus.COMPLETED);
        long pendingPayments = paymentRepository.countByBusinessIdAndStatus(businessId, PaymentStatus.PENDING);
        long failedPayments = paymentRepository.countByBusinessIdAndStatus(businessId, PaymentStatus.FAILED);

        // Get financial data
        BigDecimal totalRevenue = getBusinessTotalPaidAmount(businessId);
        
        // Calculate monthly and yearly revenue
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime startOfYear = now.withDayOfYear(1).withHour(0).withMinute(0).withSecond(0);
        
        BigDecimal monthlyRevenue = paymentRepository.getTotalRevenueInPeriod(startOfMonth, now);
        BigDecimal yearlyRevenue = paymentRepository.getTotalRevenueInPeriod(startOfYear, now);
        
        return paymentMapper.toSummaryResponse(
                totalPayments, completedPayments, pendingPayments, failedPayments, 0L,
                totalRevenue, monthlyRevenue, yearlyRevenue, 
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                paymentRepository.getAveragePaymentAmount()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPlanPayments(UUID planId) {
        List<Payment> payments = paymentRepository.findByPlanId(planId);
        return paymentMapper.toResponseList(payments);
    }

    @Override
    @Transactional(readOnly = true)
    public long getPlanPaymentCount(UUID planId) {
        return paymentRepository.countCompletedPaymentsByPlan(planId);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getPlanTotalRevenue(UUID planId) {
        return paymentRepository.findByPlanId(planId).stream()
                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentSummaryResponse getOverallPaymentSummary() {
        // Implementation for overall summary
        long totalPayments = paymentRepository.count();
        long completedPayments = paymentRepository.countByStatus(PaymentStatus.COMPLETED);
        long pendingPayments = paymentRepository.countByStatus(PaymentStatus.PENDING);
        long failedPayments = paymentRepository.countByStatus(PaymentStatus.FAILED);
        long overduePayments = paymentRepository.countOverduePayments(LocalDateTime.now());

        BigDecimal totalRevenue = getTotalRevenue();
        BigDecimal monthlyRevenue = getMonthlyRevenue();
        BigDecimal yearlyRevenue = getYearlyRevenue();

        return paymentMapper.toSummaryResponse(
                totalPayments, completedPayments, pendingPayments, failedPayments, overduePayments,
                totalRevenue, monthlyRevenue, yearlyRevenue,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                paymentRepository.getAveragePaymentAmount()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentSummaryResponse getPaymentSummaryByDateRange(String startDate, String endDate) {
        LocalDateTime start = LocalDateTime.parse(startDate + "T00:00:00");
        LocalDateTime end = LocalDateTime.parse(endDate + "T23:59:59");
        
        List<Payment> payments = paymentRepository.findPaymentsByDateRange(start, end);
        
        long totalPayments = payments.size();
        long completedPayments = payments.stream().mapToLong(p -> p.isCompleted() ? 1 : 0).sum();
        long pendingPayments = payments.stream().mapToLong(p -> p.isPending() ? 1 : 0).sum();
        long failedPayments = payments.stream().mapToLong(p -> p.isFailed() ? 1 : 0).sum();
        
        BigDecimal totalRevenue = payments.stream()
                .filter(Payment::isCompleted)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return paymentMapper.toSummaryResponse(
                totalPayments, completedPayments, pendingPayments, failedPayments, 0L,
                totalRevenue, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                0.0
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getOverduePayments() {
        List<Payment> overduePayments = paymentRepository.findOverduePayments(LocalDateTime.now());
        return paymentMapper.toResponseList(overduePayments);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getRecentPayments(int limit) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(0, limit);
        Page<Payment> recentPayments = paymentRepository.findRecentPayments(pageable);
        return paymentMapper.toResponseList(recentPayments.getContent());
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
    public BigDecimal getYearlyRevenue() {
        return getTotalRevenue(); // Same as total revenue for current implementation
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getRevenueByDateRange(String startDate, String endDate) {
        LocalDateTime start = LocalDateTime.parse(startDate + "T00:00:00");
        LocalDateTime end = LocalDateTime.parse(endDate + "T23:59:59");
        BigDecimal revenue = paymentRepository.getTotalRevenueInPeriod(start, end);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalPaymentsCount() {
        return paymentRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long getPendingPaymentsCount() {
        return paymentRepository.countByStatus(PaymentStatus.PENDING);
    }

    @Override
    @Transactional(readOnly = true)
    public long getCompletedPaymentsCount() {
        return paymentRepository.countByStatus(PaymentStatus.COMPLETED);
    }

    @Override
    @Transactional(readOnly = true)
    public long getFailedPaymentsCount() {
        return paymentRepository.countByStatus(PaymentStatus.FAILED);
    }

    @Override
    @Transactional(readOnly = true)
    public long getOverduePaymentsCount() {
        return paymentRepository.countOverduePayments(LocalDateTime.now());
    }

    @Override
    public String generateReferenceNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.valueOf((int)(Math.random() * 10000));
        return "PAY-" + timestamp + "-" + String.format("%04d", Integer.parseInt(random));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isReferenceNumberUnique(String referenceNumber) {
        return !paymentRepository.existsByReferenceNumberAndIsDeletedFalse(referenceNumber);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByReferenceNumber(String referenceNumber) {
        Payment payment = paymentRepository.findByReferenceNumberAndIsDeletedFalse(referenceNumber)
                .orElseThrow(() -> new NotFoundException("Payment not found with reference number: " + referenceNumber));
        return paymentMapper.toResponse(payment);
    }

    @Override
    public void processSubscriptionPayment(UUID subscriptionId, UUID businessId, UUID planId) {
        try {
            // Get plan details for amount
            SubscriptionPlan plan = subscriptionPlanRepository.findByIdAndIsDeletedFalse(planId)
                    .orElseThrow(() -> new NotFoundException("Plan not found"));

            // Create payment request
            PaymentCreateRequest request = new PaymentCreateRequest();
            request.setBusinessId(businessId);
            request.setSubscriptionId(subscriptionId);
            request.setPlanId(planId);
            request.setAmount(plan.getPrice());
            request.setPaymentMethod(com.emenu.enums.PaymentMethod.BANK_TRANSFER); // Default
            request.setAutoComplete(plan.isFree()); // Auto-complete free plans
            request.setNotes("Subscription payment for " + plan.getName());

            recordPayment(request);
            log.info("Subscription payment processed for subscription: {}", subscriptionId);
        } catch (Exception e) {
            log.error("Failed to process subscription payment for subscription: {}", subscriptionId, e);
            throw new RuntimeException("Failed to process subscription payment", e);
        }
    }

    @Override
    public void extendSubscriptionAfterPayment(UUID paymentId) {
        try {
            Payment payment = findPaymentById(paymentId);
            
            if (!payment.isCompleted()) {
                log.warn("Cannot extend subscription for non-completed payment: {}", paymentId);
                return;
            }

            SubscriptionPlan plan = subscriptionPlanRepository.findByIdAndIsDeletedFalse(payment.getPlanId())
                    .orElseThrow(() -> new NotFoundException("Plan not found"));

            // Check for existing active subscription
            var existingSubscription = subscriptionRepository.findCurrentActiveByBusinessId(
                    payment.getBusinessId(), LocalDateTime.now());

            if (existingSubscription.isPresent()) {
                // Extend existing subscription
                Subscription subscription = existingSubscription.get();
                subscription.extendByDays(plan.getDurationDays());
                subscriptionRepository.save(subscription);
                log.info("Extended existing subscription for business: {}", payment.getBusinessId());
            } else {
                // Create new subscription
                Subscription newSubscription = new Subscription();
                newSubscription.setBusinessId(payment.getBusinessId());
                newSubscription.setPlanId(plan.getId());
                newSubscription.setStartDate(LocalDateTime.now());
                newSubscription.setEndDate(LocalDateTime.now().plusDays(plan.getDurationDays()));
                newSubscription.setIsActive(true);
                newSubscription.setAutoRenew(false);
                newSubscription.setNotes("Created from payment: " + payment.getReferenceNumber());

                Subscription savedSubscription = subscriptionRepository.save(newSubscription);

                // Update payment with subscription ID
                payment.setSubscriptionId(savedSubscription.getId());
                paymentRepository.save(payment);

                log.info("Created new subscription for business: {}", payment.getBusinessId());
            }
        } catch (Exception e) {
            log.error("Failed to extend subscription after payment: {}", paymentId, e);
        }
    }

    // Private helper methods
    private Payment findPaymentById(UUID id) {
        return paymentRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Payment not found"));
    }

    private void updatePaymentFromProcessRequest(Payment payment, PaymentProcessRequest request) {
        if (request.getAdminNotes() != null) {
            payment.setAdminNotes(request.getAdminNotes());
        }
        if (request.getPaymentProofUrl() != null) {
            payment.setPaymentProofUrl(request.getPaymentProofUrl());
        }
    }
}