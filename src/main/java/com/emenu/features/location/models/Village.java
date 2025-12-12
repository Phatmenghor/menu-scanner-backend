package com.emenu.features.location.models;

import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "location_village_cbc",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_village_code", columnNames = "village_code")
        },
        indexes = {
                @Index(name = "idx_village_code", columnList = "village_code"),
                @Index(name = "idx_village_commune", columnList = "commune_code"),
                @Index(name = "idx_village_deleted", columnList = "is_deleted")
        }
)
public class Village extends BaseUUIDEntity {
    @Column(name = "village_code", unique = true, nullable = false, length = 10)
    private String villageCode;
    
    @Column(name = "village_en", nullable = false)
    private String villageEn;
    
    @Column(name = "village_kh", nullable = false)
    private String villageKh;
    
    @Column(name = "commune_code", nullable = false, length = 10)
    private String communeCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commune_code", referencedColumnName = "commune_code",
            insertable = false, updatable = false)
    private Commune commune;
}