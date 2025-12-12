package com.emenu.features.location.models;

import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "communes")
public class Commune extends BaseUUIDEntity {
    @Column(name = "commune_code", unique = true, nullable = false)
    private String communeCode;
    
    @Column(name = "commune_en", nullable = false)
    private String communeEn;
    
    @Column(name = "commune_kh", nullable = false)
    private String communeKh;
    
    @Column(name = "district_code")
    private String districtCode;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_code", referencedColumnName = "district_code",
                insertable = false, updatable = false)
    private District district;
}