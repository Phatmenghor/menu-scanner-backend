package com.emenu.features.subdomain.models;

import com.emenu.enums.subdomain.SubdomainStatus;
import com.emenu.features.auth.models.Business;
import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "subdomains", indexes = {
        // ✅ EXISTING: Keep current critical indexes
        @Index(name = "idx_subdomain_unique", columnList = "subdomain", unique = true),
        @Index(name = "idx_business_id", columnList = "businessId"),
        @Index(name = "idx_subdomain_status", columnList = "status"),

        // ✅ FIXED: BaseUUIDEntity indexes
        @Index(name = "idx_subdomain_deleted", columnList = "is_deleted"),
        @Index(name = "idx_subdomain_deleted_created", columnList = "is_deleted, created_at"),
        @Index(name = "idx_subdomain_business_deleted", columnList = "business_id, is_deleted"),
        @Index(name = "idx_subdomain_status_deleted", columnList = "status, is_deleted"),
        @Index(name = "idx_subdomain_access_count_deleted", columnList = "access_count, is_deleted"),
        @Index(name = "idx_subdomain_last_accessed_deleted", columnList = "last_accessed, is_deleted")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Subdomain extends BaseUUIDEntity {

    @Column(name = "subdomain", nullable = false, unique = true, length = 63)
    private String subdomain; // e.g., "myrestaurant" from myrestaurant.menu.com

    @Column(name = "business_id", nullable = false)
    private UUID businessId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", insertable = false, updatable = false)
    private Business business;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SubdomainStatus status = SubdomainStatus.ACTIVE;

    @Column(name = "last_accessed")
    private LocalDateTime lastAccessed;

    @Column(name = "access_count", nullable = false)
    private Long accessCount = 0L;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // Business Methods
    public String getFullDomain() {
        return subdomain + ".menu.com";
    }

    public String getFullUrl() {
        return "https://" + getFullDomain(); // Always HTTPS for simplicity
    }

    public boolean isAccessible() {
        return status == SubdomainStatus.ACTIVE && !getIsDeleted();
    }

    public void incrementAccess() {
        this.accessCount++;
        this.lastAccessed = LocalDateTime.now();
    }

    public boolean hasActiveSubscription() {
        return business != null && business.hasActiveSubscription();
    }

    public boolean canAccess() {
        return isAccessible() && hasActiveSubscription();
    }

    public void activate() {
        this.status = SubdomainStatus.ACTIVE;
    }

    public void suspend(String reason) {
        this.status = SubdomainStatus.SUSPENDED;
        this.notes = (notes != null ? notes + "\n" : "") + "Suspended: " + reason;
    }
}
