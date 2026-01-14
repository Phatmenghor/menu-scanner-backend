package com.emenu.features.hr.models;

import com.emenu.enums.hr.CheckInType;
import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_check_ins", indexes = {
    @Index(name = "idx_aci_attendance", columnList = "attendance_id"),
    @Index(name = "idx_aci_type", columnList = "check_in_type")
})
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceCheckIn extends BaseUUIDEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_id", nullable = false)
    private Attendance attendance;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "check_in_type", nullable = false)
    private CheckInType checkInType;
    
    @Column(name = "check_in_time", nullable = false)
    private LocalDateTime checkInTime;
    
    @Column(name = "latitude", nullable = false)
    private Double latitude;
    
    @Column(name = "longitude", nullable = false)
    private Double longitude;
    
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
}