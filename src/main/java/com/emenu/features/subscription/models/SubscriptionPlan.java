package com.emenu.features.subscription.models;

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
@Table(name = "subscription_plans", indexes = {
        // ✅ FIXED: BaseUUIDEntity indexes with UNIQUE NAMES
        @Index(name = "idx_subscription_plan_entity_deleted", columnList = "is_deleted"),
        @Index(name = "idx_subscription_plan_entity_deleted_created", columnList = "is_deleted, created_at"),

        // ✅ FIXED: Plan management indexes with UNIQUE NAMES
        @Index(name = "idx_subscription_plan_entity_status_deleted", columnList = "status, is_deleted"),
        @Index(name = "idx_subscription_plan_entity_name_deleted", columnList = "name, is_deleted"),
        @Index(name = "idx_subscription_plan_entity_price_deleted", columnList = "price, is_deleted"),
        @Index(name = "idx_subscription_plan_entity_duration_deleted", columnList = "duration_days, is_deleted"),
        @Index(name = "idx_subscription_plan_entity_price_duration_deleted", columnList = "price, duration_days, is_deleted")
})
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

}