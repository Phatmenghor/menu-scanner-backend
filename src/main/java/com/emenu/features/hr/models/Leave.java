package com.emenu.features.hr.models;

import com.emenu.enums.hr.LeaveStatusEnum;
import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "leaves", indexes = {
    @Index(name = "idx_leave_user", columnList = "user_id"),
    @Index(name = "idx_leave_business", columnList = "business_id"),
    @Index(name = "idx_leave_policy", columnList = "policy_id"),
    @Index(name = "idx_leave_status", columnList = "status")
})
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Leave extends BaseUUIDEntity {
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "business_id", nullable = false)
    private UUID businessId;
    
    @Column(name = "policy_id", nullable = false)
    private UUID policyId;
    
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;
    
    @Column(name = "total_days")
    private Double totalDays;
    
    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private LeaveStatusEnum status = LeaveStatusEnum.PENDING;
    
    @Column(name = "approved_by")
    private UUID approvedBy;
    
    @Column(name = "approved_at")
    private ZonedDateTime approvedAt;
    
    @Column(name = "approver_note", columnDefinition = "TEXT")
    private String approverNote;
}