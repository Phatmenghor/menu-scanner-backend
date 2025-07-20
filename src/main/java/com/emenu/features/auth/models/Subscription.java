package com.emenu.features.auth.models;

import com.emenu.enums.SubscriptionPlan;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "plan", nullable = false)
    private SubscriptionPlan plan;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "auto_renew", nullable = false)
    private Boolean autoRenew = false;

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
        if (plan.getMaxStaff() == -1) return true; // Unlimited
        return currentStaffCount < plan.getMaxStaff();
    }

    public boolean canAddMenuItem(int currentMenuItems) {
        if (plan.getMaxMenuItems() == -1) return true; // Unlimited
        return currentMenuItems < plan.getMaxMenuItems();
    }

    public boolean canAddTable(int currentTables) {
        if (plan.getMaxTables() == -1) return true; // Unlimited
        return currentTables < plan.getMaxTables();
    }
}