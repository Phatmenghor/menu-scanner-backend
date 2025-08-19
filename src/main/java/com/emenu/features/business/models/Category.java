package com.emenu.features.business.models;

import com.emenu.enums.common.Status;
import com.emenu.features.auth.models.Business;
import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "categories", indexes = {
        // ✅ FIXED: BaseUUIDEntity indexes
        @Index(name = "idx_category_deleted", columnList = "is_deleted"),
        @Index(name = "idx_category_deleted_created", columnList = "is_deleted, created_at"),

        // ✅ FIXED: Business relationship indexes
        @Index(name = "idx_category_business_deleted", columnList = "business_id, is_deleted"),
        @Index(name = "idx_category_status_deleted", columnList = "status, is_deleted"),
        @Index(name = "idx_category_business_status_deleted", columnList = "business_id, status, is_deleted"),
        @Index(name = "idx_category_name_deleted", columnList = "name, is_deleted")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Category extends BaseUUIDEntity {

    @Column(name = "business_id", nullable = false)
    private UUID businessId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", insertable = false, updatable = false)
    private Business business;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "image_url")
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.ACTIVE;

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