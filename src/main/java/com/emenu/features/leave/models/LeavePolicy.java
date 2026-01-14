package com.emenu.features.leave.models;

import com.emenu.enums.leave.LeaveType;
import com.emenu.features.auth.models.Business;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;

@Entity
@Table(name = "leave_policies")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeavePolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Column(nullable = false)
    private String policyName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaveType leaveType;

    @Column(nullable = false)
    private Double annualAllowance; // Total days allowed per year

    @Column(nullable = false)
    private Boolean allowHalfDay; // Allow half-day leaves

    @Column(nullable = false)
    private Boolean requiresApproval; // Whether leave requires approval

    @Column(nullable = false)
    private Integer minAdvanceNoticeDays; // Minimum days in advance to request

    @Column(nullable = false)
    private Double maxConsecutiveDays; // Maximum consecutive days allowed

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Boolean isActive;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private ZonedDateTime updatedAt;
}
