package com.menghor.ksit.feature.survey.model;

import com.menghor.ksit.enumations.StatusSurvey;
import com.menghor.ksit.utils.database.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "survey_sections")
public class SurveySectionEntity extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer displayOrder = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusSurvey status = StatusSurvey.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", nullable = false)
    private SurveyEntity survey;

    // All questions for this section including deleted ones
    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = false, fetch = FetchType.LAZY)
    @OrderBy("displayOrder ASC, id ASC")
    private List<SurveyQuestionEntity> questions = new ArrayList<>();

    // Helper method to get only active questions
    public List<SurveyQuestionEntity> getActiveQuestions() {
        return questions.stream()
                .filter(question -> question.getStatus() == StatusSurvey.ACTIVE)
                .toList();
    }

    // Helper method to get all questions (including deleted)
    public List<SurveyQuestionEntity> getAllQuestions() {
        return questions;
    }
}