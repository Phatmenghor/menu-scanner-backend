package com.menghor.ksit.feature.school.model;

import com.menghor.ksit.enumations.RequestStatus;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.utils.database.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "request_history")
public class RequestHistoryEntity extends BaseEntity {

    private String title; // Request title

    @Enumerated(EnumType.STRING)
    private RequestStatus fromStatus; // Previous status

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus toStatus; // New status

    @Column(columnDefinition = "TEXT")
    private String requestComment; // User's comment/note

    @Column(columnDefinition = "TEXT")
    private String staffComment; // Staff's comment when processing

    @Column(columnDefinition = "TEXT")
    private String comment; // Comment about the status change

    @Column(nullable = false)
    private String actionBy; // Who performed the action (username) - kept for backward compatibility

    // The original request (we can get request owner through request.user)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private RequestEntity request;

    // RENAMED: This is the user who PERFORMED the action (not the request owner)
    // Changed from 'user' to 'actionUser' for clarity
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_user_id", nullable = false)
    private UserEntity actionUser; // User who performed this action (staff/student/admin)
}