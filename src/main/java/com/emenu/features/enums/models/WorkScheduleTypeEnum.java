package com.emenu.features.enums.models;

import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "work_schedule_type_enum", indexes = {
    @Index(name = "idx_wste_business", columnList = "business_id"),
    @Index(name = "idx_wste_name", columnList = "enum_name,business_id")
})
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkScheduleTypeEnum extends BaseUUIDEntity {
    
    @Column(name = "business_id", nullable = false)
    private UUID businessId;
    
    @Column(name = "enum_name", nullable = false, length = 100)
    private String enumName;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}