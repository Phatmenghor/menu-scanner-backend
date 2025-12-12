package com.emenu.features.location.models;

import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "location_district_cbc", indexes = {
    @Index(name = "idx_district_code", columnList = "district_code"),
    @Index(name = "idx_district_province", columnList = "province_code"),
    @Index(name = "idx_district_deleted", columnList = "is_deleted")
})
public class District extends BaseUUIDEntity {
    @Column(name = "district_code", unique = true, nullable = false, length = 10)
    private String districtCode;
    
    @Column(name = "district_en", nullable = false)
    private String districtEn;
    
    @Column(name = "district_kh", nullable = false)
    private String districtKh;
    
    @Column(name = "province_code", nullable = false, length = 10)
    private String provinceCode;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "province_code", referencedColumnName = "province_code",
                insertable = false, updatable = false, 
                foreignKey = @ForeignKey(name = "fk_district_province"))
    private Province province;
}