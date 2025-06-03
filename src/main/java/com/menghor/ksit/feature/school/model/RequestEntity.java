package com.menghor.ksit.feature.school.model;

import com.menghor.ksit.enumations.RequestStatus;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.utils.database.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "requests")
public class RequestEntity extends BaseEntity {
    
    @Column(nullable = false)
    private String title; // Request title
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status = RequestStatus.PENDING; // Current status
    
    @Column(columnDefinition = "TEXT")
    private String requestComment; // User's comment/note
    
    @Column(columnDefinition = "TEXT")
    private String staffComment; // Staff's comment when processing
    
    // User who made the request
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
    
    // Request history/audit trail
    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<RequestHistoryEntity> history = new ArrayList<>();
}