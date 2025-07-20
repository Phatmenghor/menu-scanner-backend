package com.emenu.features.auth.models;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", insertable = false, updatable = false)
    private Business business;

    // Dynamic subscription plan reference
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

    // Custom limits for this specific subscription (overrides plan defaults)
    @Column(name = "custom_max_staff")
    private Integer customMaxStaff;

    @Column(name = "custom_max_menu_items")
    private Integer customMaxMenuItems;

    @Column(name = "custom_max_tables")
    private Integer customMaxTables;

    // Custom duration (overrides plan default)
    @Column(name = "custom_duration_days")
    private Integer customDurationDays;

    // Subscription notes/comments
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // Trial subscription
    @Column(name = "is_trial")
    private Boolean isTrial = false;

    @OneToMany(mappedBy = "subscription", cascade = CascadeType.ALL)
    private List<Payment> payments;

    // Business methods
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(endDate);
    }

    public boolean isCurrentlyActive() {
        return isActive && !isExpired();
    }

    public long getDaysRemaining() {
        if (isExpired()) return 0;
        return java.time.Duration.between(LocalDateTime.now(), endDate).toDays();
    }

    public boolean canAddStaff(int currentStaffCount) {
        int maxStaff = getEffectiveMaxStaff();
        if (maxStaff == -1) return true; // Unlimited
        return currentStaffCount < maxStaff;
    }

    public boolean canAddMenuItem(int currentMenuItems) {
        int maxMenuItems = getEffectiveMaxMenuItems();
        if (maxMenuItems == -1) return true; // Unlimited
        return currentMenuItems < maxMenuItems;
    }

    public boolean canAddTable(int currentTables) {
        int maxTables = getEffectiveMaxTables();
        if (maxTables == -1) return true; // Unlimited
        return currentTables < maxTables;
    }

    // Helper methods to get effective limits (custom overrides plan defaults)
    public int getEffectiveMaxStaff() {
        if (customMaxStaff != null) return customMaxStaff;
        return plan != null ? plan.getMaxStaff() : 0;
    }

    public int getEffectiveMaxMenuItems() {
        if (customMaxMenuItems != null) return customMaxMenuItems;
        return plan != null ? plan.getMaxMenuItems() : 0;
    }

    public int getEffectiveMaxTables() {
        if (customMaxTables != null) return customMaxTables;
        return plan != null ? plan.getMaxTables() : 0;
    }

    public int getEffectiveDurationDays() {
        if (customDurationDays != null) return customDurationDays;
        return plan != null ? plan.getDurationDays() : 30;
    }

    // Check if subscription is expiring soon (within specified days)
    public boolean isExpiringSoon(int days) {
        if (isExpired()) return false;
        return getDaysRemaining() <= days;
    }

    // Get display name for this subscription
    public String getDisplayName() {
        if (plan != null) {
            String planName = plan.getDisplayName();
            if (isTrial) {
                planName += " (Trial)";
            }
            if (hasCustomLimits()) {
                planName += " (Custom)";
            }
            return planName;
        }
        return "Unknown Plan";
    }

    // Check if this subscription has custom limits
    public boolean hasCustomLimits() {
        return customMaxStaff != null || customMaxMenuItems != null || 
               customMaxTables != null || customDurationDays != null;
    }

    // Extend subscription by specified days
    public void extendByDays(int days) {
        this.endDate = this.endDate.plusDays(days);
    }

    // Extend subscription by plan duration
    public void extendByPlanDuration() {
        extendByDays(getEffectiveDurationDays());
    }

    // Cancel subscription (don't delete, just deactivate)
    public void cancel() {
        this.isActive = false;
        this.autoRenew = false;
    }

    // Reactivate cancelled subscription
    public void reactivate() {
        this.isActive = true;
    }
}