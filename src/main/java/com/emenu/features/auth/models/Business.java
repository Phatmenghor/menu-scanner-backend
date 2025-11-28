package com.emenu.features.auth.models;

import com.emenu.enums.user.BusinessStatus;
import com.emenu.features.subscription.models.Subscription;
import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Entity
@Table(name = "businesses", indexes = {
        @Index(name = "idx_business_deleted", columnList = "is_deleted"),
        @Index(name = "idx_business_status", columnList = "status, is_deleted"),
        @Index(name = "idx_business_subscription", columnList = "is_subscription_active, is_deleted")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class Business extends BaseUUIDEntity {

    // Core Business Info
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "address")
    private String address;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // Status
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BusinessStatus status = BusinessStatus.PENDING;

    // Subscription Status (Only Active Flag)
    @Column(name = "is_subscription_active")
    private Boolean isSubscriptionActive = false;

    // Relationships
    @OneToMany(mappedBy = "business", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<User> users;

    @OneToOne(mappedBy = "business", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private BusinessSetting businessSetting;

    @OneToMany(mappedBy = "business", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Subscription> subscriptions;

    // Business Methods
    public boolean isActive() {
        return BusinessStatus.ACTIVE.equals(status);
    }

    public boolean hasActiveSubscription() {
        log.debug("Checking active subscription for business: {}", this.getId());

        // Check database flag
        if (Boolean.TRUE.equals(isSubscriptionActive)) {
            log.debug("Active subscription found via database flag");
            return true;
        }

        // Check subscriptions collection
        if (subscriptions != null && !subscriptions.isEmpty()) {
            boolean hasActive = subscriptions.stream()
                    .anyMatch(sub -> sub.getIsActive() && !sub.isExpired());

            if (hasActive) {
                log.debug("Active subscription found in collection");
                return true;
            }
        }

        log.debug("No active subscription found");
        return false;
    }

    public void activateSubscription() {
        log.info("Activating subscription for business: {}", this.getId());
        this.isSubscriptionActive = true;
        this.status = BusinessStatus.ACTIVE;
    }

    public void deactivateSubscription() {
        log.info("Deactivating subscription for business: {}", this.getId());
        this.isSubscriptionActive = false;
        this.status = BusinessStatus.SUSPENDED;
    }
}
