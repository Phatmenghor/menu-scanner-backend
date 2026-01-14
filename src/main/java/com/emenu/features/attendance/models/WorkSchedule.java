package com.emenu.features.attendance.models;

import com.emenu.enums.attendance.WorkScheduleType;
import com.emenu.features.auth.models.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Set;

@Entity
@Table(name = "work_schedules")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "policy_id", nullable = false)
    private AttendancePolicy attendancePolicy;

    @Column(nullable = false)
    private String scheduleName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkScheduleType scheduleType;

    // Working days (e.g., MONDAY, TUESDAY, SATURDAY, SUNDAY)
    @ElementCollection(targetClass = DayOfWeek.class)
    @CollectionTable(name = "work_schedule_days", joinColumns = @JoinColumn(name = "schedule_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "work_day")
    private Set<DayOfWeek> workDays;

    // Custom shift timing (if different from policy)
    private LocalTime customStartTime;
    private LocalTime customEndTime;

    @Column(nullable = false)
    private Boolean isActive;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private ZonedDateTime updatedAt;
}
