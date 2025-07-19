package com.emenu.features.user_management.models;

import com.emenu.enums.BusinessStatus;
import com.emenu.enums.SubscriptionPlan;
import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "businesses")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Business extends BaseUUIDEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "address")
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private BusinessStatus status = BusinessStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_plan")
    private SubscriptionPlan subscriptionPlan = SubscriptionPlan.FREE;

    @Column(name = "subscription_starts")
    private LocalDateTime subscriptionStarts;

    @Column(name = "subscription_ends")
    private LocalDateTime subscriptionEnds;

    @OneToMany(mappedBy = "business", cascade = CascadeType.ALL)
    private List<User> users;

    @Column(name = "notes")
    private String notes;

    // Convenience methods
    public boolean isActive() {
        return BusinessStatus.ACTIVE.equals(status);
    }

    public boolean isSubscriptionActive() {
        return subscriptionEnds != null && subscriptionEnds.isAfter(LocalDateTime.now());
    }
}