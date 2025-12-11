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
import java.util.UUID;

@Entity
@Table(name = "businesses", indexes = {
        @Index(name = "idx_business_deleted", columnList = "is_deleted"),
        @Index(name = "idx_business_status", columnList = "status, is_deleted"),
        @Index(name = "idx_business_subscription", columnList = "is_subscription_active, is_deleted"),
        @Index(name = "idx_business_owner", columnList = "owner_id, is_deleted")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class Business extends BaseUUIDEntity {

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

    @Column(name = "owner_id")
    private UUID ownerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BusinessStatus status = BusinessStatus.PENDING;

    @Column(name = "is_subscription_active")
    private Boolean isSubscriptionActive = false;

    @OneToMany(mappedBy = "business", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Subscription> subscriptions;

    public boolean isActive() {
        return BusinessStatus.ACTIVE.equals(status);
    }

    public boolean hasActiveSubscription() {
        if (Boolean.TRUE.equals(isSubscriptionActive)) {
            return true;
        }
        if (subscriptions != null && !subscriptions.isEmpty()) {
            return subscriptions.stream().anyMatch(sub -> sub.isActive());
        }
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