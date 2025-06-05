package com.menghor.ksit.feature.attendance.models;

import com.menghor.ksit.enumations.GradeLevel;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.utils.database.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "student_scores")
@Getter
@Setter
public class StudentScoreEntity extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "score_session_id", nullable = false)
    private ScoreSessionEntity scoreSession;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private UserEntity student;

    // Raw scores (out of 100 each) - RENAMED FROM OLD COLUMNS
    @Column(name = "attendance_raw_score", precision = 5, scale = 2)
    private BigDecimal attendanceRawScore; // 0-100

    @Column(name = "assignment_raw_score", precision = 5, scale = 2)
    private BigDecimal assignmentRawScore; // 0-100

    @Column(name = "midterm_raw_score", precision = 5, scale = 2)
    private BigDecimal midtermRawScore; // 0-100

    @Column(name = "final_raw_score", precision = 5, scale = 2)
    private BigDecimal finalRawScore; // 0-100

    @Column(name = "comments")
    private String comments;

    // Configuration reference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "score_config_id")
    private ScoreConfigurationEntity scoreConfiguration;

    // Computed weighted scores based on configuration
    @Transient
    public BigDecimal getAttendanceScore() {
        if (attendanceRawScore == null || scoreConfiguration == null) {
            return BigDecimal.ZERO;
        }
        return attendanceRawScore
                .multiply(scoreConfiguration.getAttendancePercentage())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    @Transient
    public BigDecimal getAssignmentScore() {
        if (assignmentRawScore == null || scoreConfiguration == null) {
            return BigDecimal.ZERO;
        }
        return assignmentRawScore
                .multiply(scoreConfiguration.getAssignmentPercentage())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    @Transient
    public BigDecimal getMidtermScore() {
        if (midtermRawScore == null || scoreConfiguration == null) {
            return BigDecimal.ZERO;
        }
        return midtermRawScore
                .multiply(scoreConfiguration.getMidtermPercentage())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    @Transient
    public BigDecimal getFinalScore() {
        if (finalRawScore == null || scoreConfiguration == null) {
            return BigDecimal.ZERO;
        }
        return finalRawScore
                .multiply(scoreConfiguration.getFinalPercentage())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    @Transient
    public BigDecimal getTotalScore() {
        return getAttendanceScore()
                .add(getAssignmentScore())
                .add(getMidtermScore())
                .add(getFinalScore());
    }

    @Transient
    public GradeLevel getGrade() {
        BigDecimal total = getTotalScore();
        return total != null ? GradeLevel.fromScore(total.doubleValue()) : null;
    }

    // Legacy methods for backward compatibility (return Double for existing code)
    @Transient
    public Double getAttendanceScoreDouble() {
        return getAttendanceScore().doubleValue();
    }

    @Transient
    public Double getAssignmentScoreDouble() {
        return getAssignmentScore().doubleValue();
    }

    @Transient
    public Double getMidtermScoreDouble() {
        return getMidtermScore().doubleValue();
    }

    @Transient
    public Double getFinalScoreDouble() {
        return getFinalScore().doubleValue();
    }

    @Transient
    public Double getTotalScoreDouble() {
        return getTotalScore().doubleValue();
    }
}