package com.menghor.ksit.feature.school.model;

import com.menghor.ksit.enumations.RequestStatus;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.utils.database.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

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
    private String actionBy; // Who performed the action (username)
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private RequestEntity request;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user; // User who made the change
}