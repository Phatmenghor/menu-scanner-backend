package com.menghor.ksit.feature.survey.model;

import com.menghor.ksit.enumations.StatusSurvey;
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
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"user_id", "schedule_id"},
                        name = "uk_survey_response_user_schedule"
                )
        })

public class SurveyResponseEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", nullable = false)
    private SurveyEntity survey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private ScheduleEntity schedule;

    private LocalDateTime submittedAt;

    @Enumerated(EnumType.STRING)
    private StatusSurvey status = StatusSurvey.ACTIVE;

    private Boolean isCompleted = false;
    private Double overallRating;

    @Column(columnDefinition = "text") // Fixed: lowercase 'text'
    private String overallComment;

    // Store survey snapshot as JSON when submitted
    @Column(name = "survey_snapshot", columnDefinition = "text") // Fixed: lowercase 'text'
    private String surveySnapshot;

    @Column(name = "survey_title_snapshot")
    private String surveyTitleSnapshot;

    @Column(name = "survey_description_snapshot", columnDefinition = "text") // Fixed: lowercase 'text'
    private String surveyDescriptionSnapshot;

    @OneToMany(mappedBy = "response", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<SurveyAnswerEntity> answers = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (submittedAt == null) {
            submittedAt = LocalDateTime.now();
        }
    }
}