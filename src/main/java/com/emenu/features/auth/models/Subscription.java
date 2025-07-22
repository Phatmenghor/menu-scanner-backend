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

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "subscription", cascade = CascadeType.ALL)
    private List<Payment> payments;

    // Basic business methods
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

    public boolean isExpiringSoon(int days) {
        if (isExpired()) return false;
        return getDaysRemaining() <= days;
    }

    public String getDisplayName() {
        if (plan != null) {
            return plan.getName();
        }
        return "Unknown Plan";
    }

    // Basic operations
    public void extendByDays(int days) {
        this.endDate = this.endDate.plusDays(days);
    }

    public void cancel() {
        this.isActive = false;
        this.autoRenew = false;
    }

    public void reactivate() {
        this.isActive = true;
    }
}
