package com.emenu.features.attendance.models;

import com.emenu.features.auth.models.Business;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalTime;
import java.time.ZonedDateTime;

@Entity
@Table(name = "attendance_policies")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendancePolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Column(nullable = false)
    private String policyName;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Work timing settings
    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Column(nullable = false)
    private Integer lateThresholdMinutes; // Minutes after start time to mark as late

    @Column(nullable = false)
    private Integer halfDayThresholdMinutes; // Minutes required to count as half day

    // Break settings
    private LocalTime breakStartTime;
    private LocalTime breakEndTime;

    // Location tracking
    @Column(nullable = false)
    private Boolean requireLocationCheck;

    private Double officeLatitude;
    private Double officeLongitude;
    private Integer allowedRadiusMeters; // Allowed distance from office location

    // Status
    @Column(nullable = false)
    private Boolean isActive;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private ZonedDateTime updatedAt;
}
