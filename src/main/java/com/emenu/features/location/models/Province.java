package com.emenu.features.location.models;

import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "location_province_cbc", indexes = {
        @Index(name = "idx_province_deleted", columnList = "is_deleted"),
        @Index(name = "idx_province_code", columnList = "province_code, is_deleted")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Province extends BaseUUIDEntity {

    @Column(name = "province_code", unique = true, length = 50)
    private String provinceCode;

    @Column(name = "province_en", nullable = false, length = 100)
    private String provinceEn;

    @Column(name = "province_kh", nullable = false, length = 100)
    private String provinceKh;
}
