package com.menghor.ksit.feature.attendance.models;

import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.utils.database.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Entity
@Table(name = "student_scores")
@Data
@EqualsAndHashCode(callSuper = true)
public class StudentScoreEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "score_session_id", nullable = false)
    private ScoreSessionEntity scoreSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private UserEntity student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "score_configuration_id")
    private ScoreConfigurationEntity scoreConfiguration;

    // Final scores (teachers enter scores directly within percentage limits)
    @Column(name = "attendance_score", precision = 5, scale = 2)
    private BigDecimal attendanceScore = BigDecimal.ZERO;

    @Column(name = "assignment_score", precision = 5, scale = 2)
    private BigDecimal assignmentScore = BigDecimal.ZERO;

    @Column(name = "midterm_score", precision = 5, scale = 2)
    private BigDecimal midtermScore = BigDecimal.ZERO;

    @Column(name = "final_score", precision = 5, scale = 2)
    private BigDecimal finalScore = BigDecimal.ZERO;

    @Column(name = "total_score", precision = 5, scale = 2)
    private BigDecimal totalScore = BigDecimal.ZERO;

    @Column(name = "grade", length = 2)
    private String grade;

    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;

    // Calculate grade based on total score
    @PostLoad
    @PostPersist
    @PostUpdate
    public void calculateGrade() {
        if (totalScore != null) {
            double score = totalScore.doubleValue();
            if (score >= 90) this.grade = "A";
            else if (score >= 80) this.grade = "B";
            else if (score >= 70) this.grade = "C";
            else if (score >= 60) this.grade = "D";
            else this.grade = "F";
        }
    }
}