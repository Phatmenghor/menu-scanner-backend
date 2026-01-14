package com.emenu.features.hr.models;

import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "leave_balances", indexes = {
    @Index(name = "idx_lb_user", columnList = "user_id"),
    @Index(name = "idx_lb_policy", columnList = "policy_id"),
    @Index(name = "idx_lb_year", columnList = "year")
})
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveBalance extends BaseUUIDEntity {
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "policy_id", nullable = false)
    private UUID policyId;
    
    @Column(name = "year", nullable = false)
    private Integer year;
    
    @Column(name = "total_allowance")
    private Double totalAllowance;
    
    @Column(name = "used_days")
    @Builder.Default
    private Double usedDays = 0.0;
    
    @Column(name = "remaining_days")
    private Double remainingDays;
    
    @Column(name = "carried_forward_days")
    @Builder.Default
    private Double carriedForwardDays = 0.0;
}