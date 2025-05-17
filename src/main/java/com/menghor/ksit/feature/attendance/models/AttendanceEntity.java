package com.menghor.ksit.feature.attendance.models;

import com.menghor.ksit.enumations.AttendanceStatus;
import com.menghor.ksit.enumations.AttendanceType;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.utils.database.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "attendances")
@Getter
@Setter
public class AttendanceEntity extends BaseEntity {
    @Enumerated(EnumType.STRING)
    private AttendanceStatus status;
    
    @Enumerated(EnumType.STRING)
    private AttendanceType attendanceType = AttendanceType.NONE;
    
    @Column(name = "comment")
    private String comment;
    
    @Column(name = "recorded_time")
    private LocalDateTime recordedTime;
    
    @Column(name = "is_final")
    private boolean isFinal = false;
    
    @ManyToOne
    @JoinColumn(name = "student_id")
    private UserEntity student;
    
    @ManyToOne
    @JoinColumn(name = "attendance_session_id")
    private AttendanceSessionEntity attendanceSession;
}