package com.emenu.features.services.domain;

import com.emenu.enums.SubscriptionStatus;
import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_subscriptions")
@Data
@EqualsAndHashCode(callSuper = true)
public class UserSubscription extends BaseUUIDEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "plan_id", nullable = false)
    private UUID planId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SubscriptionStatus status;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "next_billing_date")
    private LocalDateTime nextBillingDate;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "stripe_subscription_id")
    private String stripeSubscriptionId;

    // Usage Tracking
    @Column(name = "current_users")
    private Integer currentUsers = 0;

    @Column(name = "current_menus")
    private Integer currentMenus = 0;

    @Column(name = "current_month_orders")
    private Integer currentMonthOrders = 0;

    @Column(name = "auto_renew")
    private Boolean autoRenew = true;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", insertable = false, updatable = false)
    private SubscriptionPlan plan;

    // Business Methods
    public boolean isActive() {
        return SubscriptionStatus.ACTIVE.equals(status) && 
               (endDate == null || endDate.isAfter(LocalDateTime.now()));
    }

    public boolean isExpired() {
        return endDate != null && endDate.isBefore(LocalDateTime.now());
    }

    public boolean canCreateUser() {
        return plan != null && (plan.getMaxUsers() == -1 || currentUsers < plan.getMaxUsers());
    }

    public boolean canCreateMenu() {
        return plan != null && (plan.getMaxMenus() == -1 || currentMenus < plan.getMaxMenus());
    }

    public boolean canProcessOrder() {
        return plan != null && (plan.getMaxOrdersPerMonth() == -1 || 
                               currentMonthOrders < plan.getMaxOrdersPerMonth());
    }
}
