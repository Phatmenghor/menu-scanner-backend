package com.emenu.features.leave.models;

import com.emenu.features.auth.models.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;

@Entity
@Table(name = "leave_balances")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveBalance {

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
    private Integer year;

    @Column(nullable = false)
    private Double totalAllowance;

    @Column(nullable = false)
    private Double usedDays;

    @Column(nullable = false)
    private Double remainingDays;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private ZonedDateTime updatedAt;
}
