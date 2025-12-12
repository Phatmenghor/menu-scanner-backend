package com.emenu.features.location.models;

import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(
        name = "location_province_cbc",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_province_code", columnNames = "province_code")
        },
        indexes = {
                @Index(name = "idx_province_code", columnList = "province_code"),
                @Index(name = "idx_province_deleted", columnList = "is_deleted")
        }
)
public class Province extends BaseUUIDEntity {
    @Column(name = "province_code", unique = true, nullable = false, length = 10)
    private String provinceCode;
    
    @Column(name = "province_en", nullable = false)
    private String provinceEn;
    
    @Column(name = "province_kh", nullable = false)
    private String provinceKh;
}