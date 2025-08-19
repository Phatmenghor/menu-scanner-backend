package com.emenu.features.customer.models;

import com.emenu.features.auth.models.User;
import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "customer_addresses", indexes = {
        // ✅ FIXED: BaseUUIDEntity indexes
        @Index(name = "idx_customer_address_deleted", columnList = "is_deleted"),
        @Index(name = "idx_customer_address_deleted_created", columnList = "is_deleted, created_at"),

        // ✅ FIXED: User relationship indexes
        @Index(name = "idx_customer_address_user_deleted", columnList = "user_id, is_deleted"),
        @Index(name = "idx_customer_address_default_deleted", columnList = "is_default, is_deleted"),
        @Index(name = "idx_customer_address_user_default_deleted", columnList = "user_id, is_default, is_deleted"),
        @Index(name = "idx_customer_address_province_deleted", columnList = "province, is_deleted"),
        @Index(name = "idx_customer_address_district_deleted", columnList = "district, is_deleted")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class CustomerAddress extends BaseUUIDEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Column(name = "village")
    private String village; // Ex: Phum Svay Dangkum

    @Column(name = "commune")
    private String commune; // Ex: Sangkat Svay Dangkum

    @Column(name = "district", nullable = false)
    private String district; // Ex: Krong Siem Reap

    @Column(name = "province", nullable = false)
    private String province; // Ex: Siem Reap

    @Column(name = "street_number")
    private String streetNumber; // Ex: Street 63 or "St. 271"

    @Column(name = "house_number")
    private String houseNumber; // Ex: "House No. 12B"

    @Column(name = "note", columnDefinition = "TEXT")
    private String note; // Optional note: "Leave with security"

    @Column(name = "latitude", precision = 10, scale = 6)
    private BigDecimal latitude; // For Google Maps

    @Column(name = "longitude", precision = 10, scale = 6)
    private BigDecimal longitude;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    // Business Methods
    public void setAsDefault() {
        this.isDefault = true;
    }

    public void unsetDefault() {
        this.isDefault = false;
    }

    public String getFullAddress() {
        StringBuilder address = new StringBuilder();
        
        if (houseNumber != null) address.append(houseNumber).append(", ");
        if (streetNumber != null) address.append(streetNumber).append(", ");
        if (village != null) address.append(village).append(", ");
        if (commune != null) address.append(commune).append(", ");
        address.append(district).append(", ");
        address.append(province);
        
        return address.toString();
    }

    public boolean hasCoordinates() {
        return latitude != null && longitude != null;
    }
}