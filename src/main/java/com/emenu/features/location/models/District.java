package com.emenu.features.location.models;

import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "districts")
public class District extends BaseUUIDEntity {
    @Column(name = "district_code", unique = true, nullable = false)
    private String districtCode;
    
    @Column(name = "district_en", nullable = false)
    private String districtEn;
    
    @Column(name = "district_kh", nullable = false)
    private String districtKh;
    
    @Column(name = "province_code")
    private String provinceCode;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "province_code", referencedColumnName = "province_code",
                insertable = false, updatable = false)
    private Province province;
}