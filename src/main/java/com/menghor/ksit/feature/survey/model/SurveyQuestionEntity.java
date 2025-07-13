package com.menghor.ksit.feature.survey.model;

import com.menghor.ksit.enumations.QuestionTypeEnum;
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
@Table(name = "survey_questions")
public class SurveyQuestionEntity extends BaseEntity {

    @Column(nullable = false, columnDefinition = "text") // Fixed: lowercase 'text'
    private String questionText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true) // Changed: Allow null temporarily for existing data
    private QuestionTypeEnum questionType;

    @Column(nullable = false)
    private Boolean required = false;

    @Column(nullable = true) // Changed: Allow null temporarily for existing data
    private Integer displayOrder = 1;

    private Integer minRating = 1;
    private Integer maxRating = 5;
    private String leftLabel;
    private String rightLabel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusSurvey status = StatusSurvey.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private SurveySectionEntity section;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SurveyAnswerEntity> answers = new ArrayList<>();
}