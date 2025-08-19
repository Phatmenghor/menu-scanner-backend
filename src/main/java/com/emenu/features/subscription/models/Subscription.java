package com.emenu.features.subscription.models;

import com.emenu.features.auth.models.Business;
import com.emenu.features.payment.models.Payment;
import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "subscriptions", indexes = {
        // ✅ FIXED: BaseUUIDEntity indexes
        @Index(name = "idx_subscription_deleted", columnList = "is_deleted"),
        @Index(name = "idx_subscription_deleted_created", columnList = "is_deleted, created_at"),
        @Index(name = "idx_subscription_deleted_updated", columnList = "is_deleted, updated_at"),

        // ✅ FIXED: Subscription management indexes - UNIQUE NAMES!
        @Index(name = "idx_subscription_business_id_deleted", columnList = "business_id, is_deleted"),
        @Index(name = "idx_subscription_plan_id_deleted", columnList = "plan_id, is_deleted"),
        @Index(name = "idx_subscription_active_deleted", columnList = "is_active, is_deleted"),
        @Index(name = "idx_subscription_business_active_deleted", columnList = "business_id, is_active, is_deleted"),
        @Index(name = "idx_subscription_end_date_deleted", columnList = "end_date, is_deleted"),
        @Index(name = "idx_subscription_start_date_deleted", columnList = "start_date, is_deleted"),
        @Index(name = "idx_subscription_dates_active_deleted", columnList = "start_date, end_date, is_active, is_deleted"),
        @Index(name = "idx_subscription_auto_renew_deleted", columnList = "auto_renew, is_deleted"),
        @Index(name = "idx_subscription_business_dates_deleted", columnList = "business_id, start_date, end_date, is_deleted")
})
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

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "auto_renew", nullable = false)
    private Boolean autoRenew = false;

    // Proper payment relationship
    @OneToMany(mappedBy = "subscription", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Payment> payments;

    // ================================
    // BUSINESS METHODS
    // ================================

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(endDate);
    }

    public long getDaysRemaining() {
        if (isExpired()) return 0;
        return java.time.Duration.between(LocalDateTime.now(), endDate).toDays();
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

    public void cancel() {
        this.isActive = false;
        this.autoRenew = false;
    }

    /**
     * Get total amount paid for this subscription
     */
    public java.math.BigDecimal getTotalPaidAmount() {
        if (payments == null || payments.isEmpty()) {
            return java.math.BigDecimal.ZERO;
        }
        
        return payments.stream()
                .filter(payment -> payment.getStatus().isCompleted())
                .map(Payment::getAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }

    /**
     * Check if subscription is fully paid
     */
    public boolean isFullyPaid() {
        if (plan == null) return false;
        
        java.math.BigDecimal totalPaid = getTotalPaidAmount();
        return totalPaid.compareTo(plan.getPrice()) >= 0;
    }

    /**
     * Get payment status summary
     */
    public String getPaymentStatusSummary() {
        if (payments == null || payments.isEmpty()) {
            return "No payments";
        }
        
        long completedCount = payments.stream()
                .filter(payment -> payment.getStatus().isCompleted())
                .count();
        
        long pendingCount = payments.stream()
                .filter(payment -> payment.getStatus().isPending())
                .count();
        
        return String.format("%d completed, %d pending", completedCount, pendingCount);
    }
}