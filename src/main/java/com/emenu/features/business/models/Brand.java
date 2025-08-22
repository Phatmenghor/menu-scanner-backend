package com.emenu.features.business.models;

import com.emenu.enums.common.Status;
import com.emenu.features.auth.models.Business;
import com.emenu.features.product.models.Product;
import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "brands", indexes = {
        // ✅ ESSENTIAL INDEXES ONLY for Brand

        // 1. Business brands (most common in product filtering)
        @Index(name = "idx_brands_business_deleted",
                columnList = "business_id, is_deleted"),

        // 2. Business brands by status
        @Index(name = "idx_brands_business_status_deleted",
                columnList = "business_id, status, is_deleted"),

        // 3. Base soft delete index
        @Index(name = "idx_brands_deleted",
                columnList = "is_deleted")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Brand extends BaseUUIDEntity {

    @Column(name = "business_id", nullable = false)
    private UUID businessId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", insertable = false, updatable = false)
    private Business business;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.ACTIVE;

    @OneToMany(mappedBy = "brand", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Product> products;

    // Business Methods
    public void activate() {
        this.status = Status.ACTIVE;
    }

    public void deactivate() {
        this.status = Status.INACTIVE;
    }

    public boolean isActive() {
        return Status.ACTIVE.equals(status);
    }
}