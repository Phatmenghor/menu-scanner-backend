package com.emenu.features.leave.models;

import com.emenu.enums.leave.LeaveSession;
import com.emenu.enums.leave.LeaveStatus;
import com.emenu.features.auth.models.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.ZonedDateTime;

@Entity
@Table(name = "leaves")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Leave {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "leave_policy_id", nullable = false)
    private LeavePolicy leavePolicy;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaveSession session; // FIRST_HALF, SECOND_HALF, FULL_DAY

    @Column(nullable = false)
    private Double totalDays; // Calculated based on session

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaveStatus status;

    @ManyToOne
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    private ZonedDateTime approvedAt;

    @Column(columnDefinition = "TEXT")
    private String approverNote;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private ZonedDateTime updatedAt;
}
