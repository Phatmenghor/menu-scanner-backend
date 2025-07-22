
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

    // Business Logic Methods
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
}