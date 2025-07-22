package com.emenu.features.auth.models;

import com.emenu.enums.sub_scription.SubscriptionPlanStatus;
import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "subscription_plans")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPlan extends BaseUUIDEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "duration_days", nullable = false)
    private Integer durationDays;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SubscriptionPlanStatus status = SubscriptionPlanStatus.PUBLIC;

    // Relationships
    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Subscription> subscriptions;

    // ✅ ADDED: Business Logic Methods
    public boolean isPublic() {
        return SubscriptionPlanStatus.PUBLIC.equals(status);
    }

    public boolean isPrivate() {
        return SubscriptionPlanStatus.PRIVATE.equals(status);
    }

    public boolean isFree() {
        return price != null && price.compareTo(BigDecimal.ZERO) == 0;
    }

    public String getPricingDisplay() {
        if (isFree()) {
            return "Free";
        }
        return String.format("$%.2f/%d days", price, durationDays);
    }

    // ✅ ADDED: Get display name (since we removed displayName field)
    public String getDisplayName() {
        return name; // For simplified version, just return name
    }

    // ✅ ADDED: Methods that might be referenced elsewhere
    public Integer getMaxStaff() {
        // Simplified: return unlimited (-1) for now
        // In future versions, you can add these as separate fields
        return -1; // -1 means unlimited
    }

    public Integer getMaxMenuItems() {
        // Simplified: return unlimited (-1) for now
        return -1; // -1 means unlimited
    }

    public Integer getMaxTables() {
        // Simplified: return unlimited (-1) for now
        return -1; // -1 means unlimited
    }

    // ✅ ADDED: Trial support (for future use)
    public Boolean getIsTrial() {
        return false; // Simplified: no trial plans for now
    }
}