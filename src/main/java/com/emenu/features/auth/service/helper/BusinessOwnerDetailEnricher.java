package com.emenu.features.auth.service.helper;

import com.emenu.enums.payment.PaymentStatus;
import com.emenu.enums.sub_scription.SubscriptionStatus;
import com.emenu.features.auth.dto.response.BusinessOwnerDetailResponse;
import com.emenu.features.auth.models.Business;
import com.emenu.features.order.models.Payment;
import com.emenu.features.order.repository.PaymentRepository;
import com.emenu.features.subscription.models.Subscription;
import com.emenu.features.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BusinessOwnerDetailEnricher {

    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository paymentRepository;

    /**
     * Enrich response with subscription and payment data
     */
    public void enrichDetailResponse(BusinessOwnerDetailResponse response, Business business) {
        if (business == null) {
            log.warn("Cannot enrich response - business is null");
            return;
        }

        // Enrich subscription data
        enrichSubscriptionData(response, business.getId());

        // Enrich payment data if subscription exists
        if (response.getCurrentSubscriptionId() != null) {
            enrichPaymentData(response, response.getCurrentSubscriptionId());
        }
    }

    /**
     * Enrich subscription information
     */
    private void enrichSubscriptionData(BusinessOwnerDetailResponse response, UUID businessId) {
        subscriptionRepository.findCurrentActiveByBusinessId(businessId, LocalDateTime.now())
                .ifPresentOrElse(
                        subscription -> populateSubscriptionInfo(response, subscription),
                        () -> response.setSubscriptionStatus(SubscriptionStatus.EXPIRED)
                );
    }

    /**
     * Populate subscription information
     */
    private void populateSubscriptionInfo(BusinessOwnerDetailResponse response, Subscription subscription) {
        response.setCurrentSubscriptionId(subscription.getId());
        response.setCurrentPlanName(subscription.getPlan().getName());
        response.setCurrentPlanPrice(subscription.getPlan().getPrice());
        response.setCurrentPlanDurationDays(subscription.getPlan().getDurationDays());
        response.setSubscriptionStartDate(subscription.getStartDate());
        response.setSubscriptionEndDate(subscription.getEndDate());
        response.setDaysRemaining(calculateDaysRemaining(subscription.getEndDate()));
        response.setDaysActive(calculateDaysActive(subscription.getStartDate()));
        response.setSubscriptionStatus(determineSubscriptionStatus(subscription));
        response.setAutoRenew(subscription.getAutoRenew());
        response.setIsExpiringSoon(subscription.isExpiringSoon(7));
    }

    /**
     * Enrich payment summary data
     */
    private void enrichPaymentData(BusinessOwnerDetailResponse response, UUID subscriptionId) {
        List<Payment> payments = paymentRepository.findBySubscriptionIdAndIsDeletedFalse(subscriptionId);

        if (payments.isEmpty()) {
            setDefaultPaymentData(response);
            return;
        }

        BigDecimal totalPaid = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPending = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.PENDING)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long completedCount = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
                .count();

        long pendingCount = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.PENDING)
                .count();

        LocalDateTime lastPaymentDate = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
                .map(Payment::getCreatedAt)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        response.setTotalPaid(totalPaid);
        response.setTotalPending(totalPending);
        response.setTotalPayments(payments.size());
        response.setCompletedPayments((int) completedCount);
        response.setPendingPayments((int) pendingCount);
        response.setPaymentStatus(determinePaymentStatus(totalPaid, totalPending, response.getCurrentPlanPrice()));
        response.setLastPaymentDate(lastPaymentDate);
    }

    /**
     * Set default payment data when no payments exist
     */
    private void setDefaultPaymentData(BusinessOwnerDetailResponse response) {
        response.setTotalPaid(BigDecimal.ZERO);
        response.setTotalPending(BigDecimal.ZERO);
        response.setTotalPayments(0);
        response.setCompletedPayments(0);
        response.setPendingPayments(0);
        response.setPaymentStatus("UNPAID");
        response.setLastPaymentDate(null);
    }

    /**
     * Calculate days remaining
     */
    private Long calculateDaysRemaining(LocalDateTime endDate) {
        if (endDate == null) return 0L;
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(endDate)) return 0L;
        return ChronoUnit.DAYS.between(now, endDate);
    }

    /**
     * Calculate days active
     */
    private Long calculateDaysActive(LocalDateTime startDate) {
        if (startDate == null) return 0L;
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(startDate)) return 0L;
        return ChronoUnit.DAYS.between(startDate, now);
    }

    /**
     * Determine subscription status using enum
     */
    private SubscriptionStatus determineSubscriptionStatus(Subscription subscription) {
        if (subscription.isExpired()) {
            return SubscriptionStatus.EXPIRED;
        }
        if (subscription.isExpiringSoon(7)) {
            return SubscriptionStatus.EXPIRING_SOON;
        }
        return SubscriptionStatus.ACTIVE;
    }

    /**
     * Determine payment status
     */
    private String determinePaymentStatus(BigDecimal totalPaid, BigDecimal totalPending, BigDecimal planPrice) {
        if (planPrice == null) {
            return "UNKNOWN";
        }

        if (totalPaid.compareTo(planPrice) >= 0) {
            return "PAID";
        } else if (totalPaid.compareTo(BigDecimal.ZERO) > 0) {
            return "PARTIALLY_PAID";
        } else if (totalPending.compareTo(BigDecimal.ZERO) > 0) {
            return "PENDING";
        }

        return "UNPAID";
    }
}


