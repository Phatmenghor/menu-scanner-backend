package com.menghor.ksit.feature.survey.model;

import com.menghor.ksit.enumations.QuestionTypeEnum;
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
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String questionText;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionTypeEnum questionType;
    
    @Column(nullable = false)
    private Boolean required = false;
    
    @Column(nullable = false)
    private Integer displayOrder = 0;
    
    // For RATING type questions
    private Integer minRating = 1;
    private Integer maxRating = 5;
    private String leftLabel;  // e.g., "Strongly Disagree"
    private String rightLabel; // e.g., "Strongly Agree"
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private SurveySectionEntity section;
    
    // Answers to this question
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SurveyAnswerEntity> answers = new ArrayList<>();
}