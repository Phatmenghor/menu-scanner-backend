package com.emenu.features.hr.models;

import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "attendances", indexes = {
    @Index(name = "idx_att_user", columnList = "user_id"),
    @Index(name = "idx_att_business", columnList = "business_id"),
    @Index(name = "idx_att_date", columnList = "attendance_date"),
    @Index(name = "idx_att_user_date", columnList = "user_id,attendance_date")
})
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Attendance extends BaseUUIDEntity {
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "business_id", nullable = false)
    private UUID businessId;
    
    @Column(name = "work_schedule_id", nullable = false)
    private UUID workScheduleId;
    
    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;
    
    @OneToMany(mappedBy = "attendance", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<AttendanceCheckIn> checkIns = new ArrayList<>();
    
    @Column(name = "total_work_minutes")
    private Integer totalWorkMinutes;
    
    @Column(name = "late_minutes")
    private Integer lateMinutes;
    
    @Column(name = "status_enum_id")
    private UUID statusEnumId;
    
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
    
    public void addCheckIn(AttendanceCheckIn checkIn) {
        checkIns.add(checkIn);
        checkIn.setAttendance(this);
    }
}