package com.emenu.features.subscription.models;

import com.emenu.enums.SubscriptionPlan;
import com.emenu.enums.SubscriptionStatus;
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
@Table(name = "subscriptions")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Subscription extends BaseUUIDEntity {

    @Column(name = "business_id", nullable = false)
    private UUID businessId;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan", nullable = false)
    private SubscriptionPlan plan;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "monthly_price", precision = 10, scale = 2)
    private Double monthlyPrice;

    @Column(name = "billing_cycle") // MONTHLY, YEARLY
    private String billingCycle = "MONTHLY";

    @Column(name = "auto_renew")
    private Boolean autoRenew = true;

    @Column(name = "trial_period")
    private Boolean trialPeriod = false;

    @Column(name = "trial_end_date")
    private LocalDateTime trialEndDate;

    // Custom limits (overrides plan defaults if set)
    @Column(name = "custom_max_staff")
    private Integer customMaxStaff;

    @Column(name = "custom_max_menu_items")
    private Integer customMaxMenuItems;

    @Column(name = "custom_max_tables")
    private Integer customMaxTables;

    // Payment info
    @OneToMany(mappedBy = "subscription", cascade = CascadeType.ALL)
    private List<Payment> payments;

    // Usage tracking
    @Column(name = "current_staff_count")
    private Integer currentStaffCount = 0;

    @Column(name = "current_menu_items")
    private Integer currentMenuItems = 0;

    @Column(name = "current_tables")
    private Integer currentTables = 0;

    // Convenience methods
    public boolean isActive() {
        return SubscriptionStatus.ACTIVE.equals(status) && 
               (endDate == null || endDate.isAfter(LocalDateTime.now()));
    }

    public boolean isInTrial() {
        return trialPeriod && trialEndDate != null && trialEndDate.isAfter(LocalDateTime.now());
    }

    public int getMaxStaff() {
        return customMaxStaff != null ? customMaxStaff : plan.getMaxStaffMembers();
    }

    public int getMaxMenuItems() {
        return customMaxMenuItems != null ? customMaxMenuItems : plan.getMaxMenuItems();
    }

    public int getMaxTables() {
        return customMaxTables != null ? customMaxTables : plan.getMaxTables();
    }

    public boolean canAddStaff() {
        return getMaxStaff() == -1 || currentStaffCount < getMaxStaff();
    }

    public boolean canAddMenuItem() {
        return getMaxMenuItems() == -1 || currentMenuItems < getMaxMenuItems();
    }

    public boolean canAddTable() {
        return getMaxTables() == -1 || currentTables < getMaxTables();
    }
}