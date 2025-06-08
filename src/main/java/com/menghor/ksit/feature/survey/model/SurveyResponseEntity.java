package com.menghor.ksit.feature.survey.model;

import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.school.model.ScheduleEntity;
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
@Table(name = "survey_responses",
        uniqueConstraints = @UniqueConstraint(columnNames = {"survey_id", "user_id", "schedule_id"}))
public class SurveyResponseEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", nullable = false)
    private SurveyEntity survey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user; // Student who submitted the response

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private ScheduleEntity schedule; // The schedule this response is for

    private LocalDateTime submittedAt;
    private LocalDateTime startedAt;
    private LocalDateTime lastUpdatedAt;

    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;

    private Boolean isCompleted = false;
    private Boolean isDraft = false;

    // Response metadata
    private String ipAddress;
    private String userAgent;
    private Integer totalTimeSpentMinutes;

    // Overall rating/score if applicable
    private Double overallRating;
    private String overallComment;

    // Individual answers
    @OneToMany(mappedBy = "response", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SurveyAnswerEntity> answers = new ArrayList<>();

    // Response history for tracking changes
    @OneToMany(mappedBy = "response", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SurveyResponseHistoryEntity> history = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (startedAt == null) {
            startedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdatedAt = LocalDateTime.now();
    }
}
