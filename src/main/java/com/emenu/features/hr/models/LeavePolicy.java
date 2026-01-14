package com.emenu.features.hr.models;

import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "leave_policies", indexes = {
    @Index(name = "idx_lp_business", columnList = "business_id"),
    @Index(name = "idx_lp_type_enum", columnList = "type_enum_id")
})
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeavePolicy extends BaseUUIDEntity {
    
    @Column(name = "business_id", nullable = false)
    private UUID businessId;
    
    @Column(name = "type_enum_id", nullable = false)
    private UUID typeEnumId;
    
    @Column(name = "policy_name", nullable = false)
    private String policyName;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "annual_allowance")
    private Double annualAllowance;
    
    @Column(name = "allow_half_day")
    @Builder.Default
    private Boolean allowHalfDay = true;
    
    @Column(name = "requires_approval")
    @Builder.Default
    private Boolean requiresApproval = true;
    
    @Column(name = "min_advance_notice_days")
    @Builder.Default
    private Integer minAdvanceNoticeDays = 0;
    
    @Column(name = "max_consecutive_days")
    @Builder.Default
    private Double maxConsecutiveDays = 30.0;
}