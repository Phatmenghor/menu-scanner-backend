package com.emenu.features.hr.models;

import com.emenu.enums.hr.LeaveStatusEnum;
import com.emenu.features.auth.models.Business;
import com.emenu.features.auth.models.User;
import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "leaves", indexes = {
    @Index(name = "idx_leave_user", columnList = "user_id"),
    @Index(name = "idx_leave_business", columnList = "business_id"),
    @Index(name = "idx_leave_status", columnList = "status")
})
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Leave extends BaseUUIDEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Column(name = "business_id", nullable = false)
    private UUID businessId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", insertable = false, updatable = false)
    private Business business;

    @Column(name = "leave_type_enum")
    private UUID leaveTypeEnum;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "total_days")
    private Double totalDays;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private LeaveStatusEnum status = LeaveStatusEnum.PENDING;

    // Action tracking (for both approval and rejection)
    @Column(name = "action_by")
    private UUID actionBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_by", insertable = false, updatable = false)
    private User actionUser;

    @Column(name = "action_at")
    private LocalDateTime actionAt;

    @Column(name = "action_note", columnDefinition = "TEXT")
    private String actionNote;
}
