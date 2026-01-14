package com.emenu.features.attendance.models;

import com.emenu.enums.attendance.AttendanceStatus;
import com.emenu.features.auth.models.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Entity
@Table(name = "attendances")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "work_schedule_id", nullable = false)
    private WorkSchedule workSchedule;

    @Column(nullable = false)
    private LocalDate attendanceDate;

    // Check-in details
    private LocalDateTime checkInTime;
    private Double checkInLatitude;
    private Double checkInLongitude;
    private String checkInAddress;
    private String checkInNote;

    // Check-out details
    private LocalDateTime checkOutTime;
    private Double checkOutLatitude;
    private Double checkOutLongitude;
    private String checkOutAddress;
    private String checkOutNote;

    // Calculated fields
    private Integer totalWorkMinutes;
    private Integer lateMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private ZonedDateTime updatedAt;
}
