package com.emenu.features.auth.service.impl;

import com.emenu.enums.payment.PaymentStatus;
import com.emenu.exception.custom.NotFoundException;
import com.emenu.exception.custom.ValidationException;
import com.emenu.features.auth.dto.filter.PaymentFilterRequest;
import com.emenu.features.auth.dto.request.PaymentCreateRequest;
import com.emenu.features.auth.dto.response.PaymentResponse;
import com.emenu.features.auth.dto.update.PaymentUpdateRequest;
import com.emenu.features.auth.mapper.PaymentMapper;
import com.emenu.features.auth.models.Business;
import com.emenu.features.auth.models.Payment;
import com.emenu.features.auth.models.SubscriptionPlan;
import com.emenu.features.auth.repository.BusinessRepository;
import com.emenu.features.auth.repository.PaymentRepository;
import com.emenu.features.auth.repository.SubscriptionPlanRepository;
import com.emenu.features.auth.service.ExchangeRateService;
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
    private final SubscriptionPlanRepository planRepository;
    private final ExchangeRateService exchangeRateService;
    private final PaymentMapper paymentMapper;
    private final SecurityUtils securityUtils;

    @Override
    public PaymentResponse createPayment(PaymentCreateRequest request) {
        log.info("Creating payment for business: {} with amount: {}", request.getBusinessId(), request.getAmount());

        // Validate business exists
        Business business = businessRepository.findByIdAndIsDeletedFalse(request.getBusinessId())
                .orElseThrow(() -> new NotFoundException("Business not found"));

        // Validate plan exists
        SubscriptionPlan plan = planRepository.findByIdAndIsDeletedFalse(request.getPlanId())
                .orElseThrow(() -> new NotFoundException("Subscription plan not found"));

        // Validate reference number uniqueness if provided
        if (request.getReferenceNumber() != null && !isReferenceNumberUnique(request.getReferenceNumber())) {
            throw new ValidationException("Reference number already exists: " + request.getReferenceNumber());
        }

        // Create payment entity
        Payment payment = paymentMapper.toEntity(request);

        // Calculate KHR amount using current exchange rate
        Double currentRate = exchangeRateService.getCurrentRate(request.getBusinessId());
        payment.calculateAmountKhr(currentRate);

        // Generate reference number if not provided
        if (payment.getReferenceNumber() == null || payment.getReferenceNumber().isEmpty()) {
            payment.setReferenceNumber(generateReferenceNumber());
        }

        Payment savedPayment = paymentRepository.save(payment);

        // Auto-complete if requested or for free plans
        if (Boolean.TRUE.equals(request.getAutoComplete()) || plan.isFree()) {
            savedPayment.markAsCompleted();
            savedPayment = paymentRepository.save(savedPayment);
            log.info("Payment auto-completed for free plan or auto-complete request");
        }

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

        // Only allow updates for pending payments
        if (!payment.getStatus().isPending()) {
            throw new ValidationException("Can only update pending payments");
        }

        // Validate reference number uniqueness if being updated
        if (request.getReferenceNumber() != null && 
            !request.getReferenceNumber().equals(payment.getReferenceNumber()) &&
            !isReferenceNumberUnique(request.getReferenceNumber())) {
            throw new ValidationException("Reference number already exists: " + request.getReferenceNumber());
        }

        paymentMapper.updateEntity(request, payment);

        // Recalculate KHR amount if amount changed
        if (request.getAmount() != null) {
            Double currentRate = exchangeRateService.getCurrentRate(payment.getBusinessId());
            payment.calculateAmountKhr(currentRate);
        }

        Payment updatedPayment = paymentRepository.save(payment);
        log.info("Payment updated successfully: {}", id);

        return paymentMapper.toResponse(updatedPayment);
    }

    @Override
    public PaymentResponse deletePayment(UUID id) {
        Payment payment = findPaymentById(id);

        // Only allow deletion of pending payments
        if (!payment.getStatus().isPending()) {
            throw new ValidationException("Can only delete pending payments");
        }

        payment.softDelete();
        payment = paymentRepository.save(payment);

        log.info("Payment deleted successfully: {}", id);
        return paymentMapper.toResponse(payment);
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
        return getAllPayments(filter);
    }

    @Override
    public PaymentResponse completePayment(UUID id, String notes) {
        Payment payment = findPaymentById(id);

        if (payment.getStatus().isCompleted()) {
            throw new ValidationException("Payment is already completed");
        }

        payment.markAsCompleted();
        
        if (notes != null && !notes.trim().isEmpty()) {
            String existingNotes = payment.getNotes() != null ? payment.getNotes() + "\n" : "";
            payment.setNotes(existingNotes + "Completed: " + notes);
        }

        Payment completedPayment = paymentRepository.save(payment);
        log.info("Payment completed successfully: {}", id);

        return paymentMapper.toResponse(completedPayment);
    }

    @Override
    public PaymentResponse cancelPayment(UUID id, String reason) {
        Payment payment = findPaymentById(id);

        if (!payment.getStatus().isPending()) {
            throw new ValidationException("Can only cancel pending payments");
        }

        payment.setStatus(PaymentStatus.CANCELLED);
        
        if (reason != null && !reason.trim().isEmpty()) {
            String existingNotes = payment.getNotes() != null ? payment.getNotes() + "\n" : "";
            payment.setNotes(existingNotes + "Cancelled: " + reason);
        }

        Payment cancelledPayment = paymentRepository.save(payment);
        log.info("Payment cancelled successfully: {}", id);

        return paymentMapper.toResponse(cancelledPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByReference(String referenceNumber) {
        Payment payment = paymentRepository.findByReferenceNumberAndIsDeletedFalse(referenceNumber)
                .orElseThrow(() -> new NotFoundException("Payment not found with reference: " + referenceNumber));
        return paymentMapper.toResponse(payment);
    }

    @Override
    public String generateReferenceNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.valueOf((int)(Math.random() * 1000));
        return "PAY-" + timestamp + "-" + String.format("%03d", Integer.parseInt(random));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isReferenceNumberUnique(String referenceNumber) {
        return !paymentRepository.existsByReferenceNumberAndIsDeletedFalse(referenceNumber);
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalPaymentsCount() {
        return paymentRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long getCompletedPaymentsCount() {
        return paymentRepository.countByStatusAndIsDeletedFalse(PaymentStatus.COMPLETED);
    }

    @Override
    @Transactional(readOnly = true)
    public long getPendingPaymentsCount() {
        return paymentRepository.countByStatusAndIsDeletedFalse(PaymentStatus.PENDING);
    }

    @Override
    @Transactional(readOnly = true)
    public long getBusinessPaymentsCount(UUID businessId) {
        return paymentRepository.countByBusinessIdAndIsDeletedFalse(businessId);
    }

    // Private helper methods
    private Payment findPaymentById(UUID id) {
        return paymentRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Payment not found"));
    }
}