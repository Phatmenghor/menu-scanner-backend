package com.menghor.ksit.feature.attendance.models;

import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.school.model.ScheduleEntity;
import com.menghor.ksit.utils.database.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "attendance_sessions")
public class AttendanceSessionEntity extends BaseEntity {

    @Column(name = "session_date", nullable = false)
    private LocalDateTime sessionDate;

    @Column(name = "qr_code", nullable = false)
    private String qrCode;

    @Column(name = "qr_expiry_time", nullable = false)
    private LocalDateTime qrExpiryTime;

    @Column(name = "is_final")
    private boolean isFinal = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.ACTIVE;

    @ManyToOne
    @JoinColumn(name = "schedule_id", nullable = false)
    private ScheduleEntity schedule;

    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    private UserEntity teacher;

    @OneToMany(mappedBy = "attendanceSession", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AttendanceEntity> attendances = new ArrayList<>();

    // Helper method to generate a new QR code
    @PrePersist
    public void generateQrCode() {
        this.qrCode = UUID.randomUUID().toString();
        // QR expires in 15 minutes
        this.qrExpiryTime = this.sessionDate.plusMinutes(15);
    }
}