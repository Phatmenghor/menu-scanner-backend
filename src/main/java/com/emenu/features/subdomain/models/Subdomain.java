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
    @Index(name = "idx_subdomain_unique", columnList = "subdomain", unique = true),
    @Index(name = "idx_business_id", columnList = "businessId"),
    @Index(name = "idx_subdomain_status", columnList = "status"),
    @Index(name = "idx_subdomain_active", columnList = "isActive")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Subdomain extends BaseUUIDEntity {

    @Column(name = "subdomain", nullable = false, unique = true, length = 63)
    private String subdomain; // e.g., "shop_domain" from shop_domain.menu.com

    @Column(name = "business_id", nullable = false)
    private UUID businessId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", insertable = false, updatable = false)
    private Business business;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SubdomainStatus status = SubdomainStatus.ACTIVE;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "domain_verified", nullable = false)
    private Boolean domainVerified = false;

    @Column(name = "ssl_enabled", nullable = false)
    private Boolean sslEnabled = false;

    @Column(name = "custom_domain")
    private String customDomain; // For future use when they have their own domain

    @Column(name = "verification_token")
    private String verificationToken;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

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
        String protocol = sslEnabled ? "https" : "http";
        return protocol + "://" + getFullDomain();
    }

    public boolean isAccessible() {
        return isActive && status == SubdomainStatus.ACTIVE && !getIsDeleted();
    }

    public void incrementAccess() {
        this.accessCount++;
        this.lastAccessed = LocalDateTime.now();
    }

    public void verify() {
        this.domainVerified = true;
        this.verifiedAt = LocalDateTime.now();
        this.verificationToken = null;
    }

    public void suspend(String reason) {
        this.status = SubdomainStatus.SUSPENDED;
        this.isActive = false;
        this.notes = (notes != null ? notes + "\n" : "") + "Suspended: " + reason;
    }

    public void activate() {
        this.status = SubdomainStatus.ACTIVE;
        this.isActive = true;
    }

    public boolean hasActiveSubscription() {
        return business != null && business.hasActiveSubscription();
    }

    public boolean canAccess() {
        return isAccessible() && hasActiveSubscription();
    }
}