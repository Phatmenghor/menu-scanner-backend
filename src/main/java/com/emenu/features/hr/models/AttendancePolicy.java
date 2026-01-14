package com.emenu.features.hr.models;

import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "attendance_policies", indexes = {
    @Index(name = "idx_ap_business", columnList = "business_id")
})
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendancePolicy extends BaseUUIDEntity {
    
    @Column(name = "business_id", nullable = false)
    private UUID businessId;
    
    @Column(name = "policy_name", nullable = false)
    private String policyName;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;
    
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;
    
    @Column(name = "late_threshold_minutes")
    private Integer lateThresholdMinutes;
    
    @Column(name = "half_day_threshold_minutes")
    private Integer halfDayThresholdMinutes;
    
    @Column(name = "default_check_ins", nullable = false)
    @Builder.Default
    private Integer defaultCheckIns = 2;
}