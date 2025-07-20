
package com.emenu.features.auth.models;

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

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "duration_days", nullable = false)
    private Integer durationDays;

    // Feature Limits (-1 means unlimited)
    @Column(name = "max_staff")
    private Integer maxStaff;

    @Column(name = "max_menu_items")
    private Integer maxMenuItems;

    @Column(name = "max_tables")
    private Integer maxTables;

    // Features (stored as JSON or comma-separated)
    @Column(name = "features", columnDefinition = "TEXT")
    private String features; // JSON array of features

    // Plan Status
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    @Column(name = "is_custom", nullable = false)
    private Boolean isCustom = false; // For custom plans created for specific businesses

    // Trial & Promotional
    @Column(name = "is_trial")
    private Boolean isTrial = false;

    @Column(name = "trial_duration_days")
    private Integer trialDurationDays;

    // Display Order
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    // Relationships
    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<com.emenu.features.auth.models.Subscription> subscriptions;

    // Business Logic Methods
    public boolean isUnlimited(String feature) {
        return switch (feature.toLowerCase()) {
            case "staff" -> maxStaff != null && maxStaff == -1;
            case "menu" -> maxMenuItems != null && maxMenuItems == -1;
            case "tables" -> maxTables != null && maxTables == -1;
            default -> false;
        };
    }

    public boolean canAddStaff(int currentCount) {
        if (maxStaff == null || maxStaff == -1) return true;
        return currentCount < maxStaff;
    }

    public boolean canAddMenuItem(int currentCount) {
        if (maxMenuItems == null || maxMenuItems == -1) return true;
        return currentCount < maxMenuItems;
    }

    public boolean canAddTable(int currentCount) {
        if (maxTables == null || maxTables == -1) return true;
        return currentCount < maxTables;
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

    // Feature list helpers
    public List<String> getFeatureList() {
        if (features == null || features.isEmpty()) {
            return List.of();
        }
        // Simple comma-separated for now, can be enhanced to JSON
        return List.of(features.split(","));
    }

    public void setFeatureList(List<String> featureList) {
        if (featureList == null || featureList.isEmpty()) {
            this.features = null;
        } else {
            this.features = String.join(",", featureList);
        }
    }
}