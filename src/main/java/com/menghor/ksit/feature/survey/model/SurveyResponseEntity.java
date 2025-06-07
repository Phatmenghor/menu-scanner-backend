package com.menghor.ksit.feature.survey.model;

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
@Table(name = "survey_responses")
public class SurveyResponseEntity extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", nullable = false)
    private SurveyEntity survey;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user; // Student who submitted the response
    
    private LocalDateTime submittedAt;
    private Boolean isCompleted = false;
    
    // Individual answers
    @OneToMany(mappedBy = "response", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SurveyAnswerEntity> answers = new ArrayList<>();
}