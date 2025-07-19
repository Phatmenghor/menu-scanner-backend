package com.emenu.features.services.domain;

import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Entity
@Table(name = "subscription_plans")
@Data
@EqualsAndHashCode(callSuper = true)
public class SubscriptionPlan extends BaseUUIDEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "currency", nullable = false)
    private String currency = "USD";

    @Column(name = "billing_cycle", nullable = false)
    private String billingCycle; // MONTHLY, YEARLY

    // Plan Limits
    @Column(name = "max_users")
    private Integer maxUsers;

    @Column(name = "max_menus")
    private Integer maxMenus;

    @Column(name = "max_orders_per_month")
    private Integer maxOrdersPerMonth;

    @Column(name = "has_analytics")
    private Boolean hasAnalytics = false;

    @Column(name = "has_reports")
    private Boolean hasReports = false;

    @Column(name = "has_custom_domain")
    private Boolean hasCustomDomain = false;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;
}