package com.emenu.features.location.models;

import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "villages")
public class Village extends BaseUUIDEntity {
    @Column(name = "village_code", unique = true, nullable = false)
    private String villageCode;
    
    @Column(name = "village_en", nullable = false)
    private String villageEn;
    
    @Column(name = "village_kh", nullable = false)
    private String villageKh;
    
    @Column(name = "commune_code")
    private String communeCode;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commune_code", referencedColumnName = "commune_code",
                insertable = false, updatable = false)
    private Commune commune;
}