package com.menghor.ksit.feature.survey.model;

import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.utils.database.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "survey_response_history")
public class SurveyResponseHistoryEntity extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "response_id", nullable = false)
    private SurveyResponseEntity response;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user; // Who made the action
    
    @Column(nullable = false)
    private String action; // CREATED, UPDATED, SUBMITTED, DELETED
    
    @Enumerated(EnumType.STRING)
    private Status fromStatus;
    
    @Enumerated(EnumType.STRING)
    private Status toStatus;
    
    @Column(columnDefinition = "TEXT")
    private String changeDetails; // JSON or description of what changed
    
    @Column(columnDefinition = "TEXT")
    private String comment;
    
    private LocalDateTime actionDate;
    
    @PrePersist
    protected void onCreate() {
        if (actionDate == null) {
            actionDate = LocalDateTime.now();
        }
    }
}