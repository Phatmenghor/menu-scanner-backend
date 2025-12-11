package com.emenu.features.subscription.models;

import com.emenu.features.auth.models.Business;
import com.emenu.features.payment.models.Payment;
import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "subscriptions")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Subscription extends BaseUUIDEntity {

    @Column(name = "business_id", nullable = false)
    private UUID businessId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", insertable = false, updatable = false)
    private Business business;

    @Column(name = "plan_id", nullable = false)
    private UUID planId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", insertable = false, updatable = false)
    private SubscriptionPlan plan;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "auto_renew", nullable = false)
    private Boolean autoRenew = false;

    @OneToMany(mappedBy = "subscription", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Payment> payments = new ArrayList<>();

    public boolean isActive() {
        return !getIsDeleted() && !isExpired();
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(endDate);
    }

    public String getStatus() {
        return isActive() ? "ACTIVE" : "EXPIRED";
    }

    public long getDaysRemaining() {
        if (isExpired()) return 0;
        return ChronoUnit.DAYS.between(LocalDateTime.now(), endDate);
    }

    public boolean isExpiringSoon(int days) {
        if (isExpired()) return false;
        long daysRemaining = getDaysRemaining();
        return daysRemaining > 0 && daysRemaining <= days;
    }

    public BigDecimal getPaymentAmount() {
        if (payments == null || payments.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return payments.stream()
                .filter(payment -> payment.getStatus().isCompleted())
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public String getPaymentStatus() {
        if (payments == null || payments.isEmpty()) {
            return "UNPAID";
        }
        boolean hasPending = payments.stream().anyMatch(p -> p.getStatus().isPending());
        if (plan != null) {
            BigDecimal totalPaid = getPaymentAmount();
            if (totalPaid.compareTo(plan.getPrice()) >= 0) {
                return "PAID";
            } else if (totalPaid.compareTo(BigDecimal.ZERO) > 0) {
                return "PARTIALLY_PAID";
            }
        }
        if (hasPending) {
            return "PENDING";
        }
        return "UNPAID";
    }

    public String getDisplayName() {
        if (plan != null) {
            return plan.getName();
        }
        return "Unknown Plan";
    }

    public void extendByDays(int days) {
        this.endDate = this.endDate.plusDays(days);
    }

    public void renew() {
        if (plan == null) {
            throw new IllegalStateException("Cannot renew subscription without plan");
        }
        LocalDateTime renewalStart = isExpired() ? LocalDateTime.now() : this.endDate;
        this.startDate = renewalStart;
        this.endDate = renewalStart.plusDays(plan.getDurationDays());
    }

    public void cancel() {
        this.autoRenew = false;
    }
}