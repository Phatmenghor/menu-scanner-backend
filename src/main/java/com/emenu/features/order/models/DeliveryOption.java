package com.emenu.features.order.models;

import com.emenu.enums.common.Status;
import com.emenu.features.auth.models.Business;
import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "delivery_options", indexes = {
        // ✅ FIXED: BaseUUIDEntity indexes
        @Index(name = "idx_delivery_option_deleted", columnList = "is_deleted"),
        @Index(name = "idx_delivery_option_deleted_created", columnList = "is_deleted, created_at"),

        // ✅ FIXED: Business relationship indexes
        @Index(name = "idx_delivery_option_business_deleted", columnList = "business_id, is_deleted"),
        @Index(name = "idx_delivery_option_status_deleted", columnList = "status, is_deleted"),
        @Index(name = "idx_delivery_option_business_status_deleted", columnList = "business_id, status, is_deleted"),
        @Index(name = "idx_delivery_option_price_deleted", columnList = "price, is_deleted")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryOption extends BaseUUIDEntity {

    @Column(name = "business_id", nullable = false)
    private UUID businessId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", insertable = false, updatable = false)
    private Business business;

    @Column(name = "name", nullable = false)
    private String name; // Ex: "Standard Delivery", "Express Delivery"

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

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