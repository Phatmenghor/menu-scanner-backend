package com.menghor.ksit.feature.attendance.models;

import com.menghor.ksit.enumations.GradeLevel;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.utils.database.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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

    @Column(name = "attendance_score")
    private Double attendanceScore; // Out of 10 points maximum

    @Column(name = "assignment_score")
    private Double assignmentScore; // Out of 20 points maximum

    @Column(name = "midterm_score")
    private Double midtermScore; // Out of 30 points maximum

    @Column(name = "final_score")
    private Double finalScore; // Out of 40 points maximum

    @Column(name = "comments")
    private String comments;

    // Transient properties (computed, not stored in database)
    @Transient
    public Double getTotalScore() {
        // Simple addition of all score components
        double total = 0.0;
        if (attendanceScore != null) total += attendanceScore;  // Max 10 points
        if (assignmentScore != null) total += assignmentScore;  // Max 20 points
        if (midtermScore != null) total += midtermScore;        // Max 30 points
        if (finalScore != null) total += finalScore;            // Max 40 points
        return total;  // Max 100 points total
    }

    @Transient
    public GradeLevel getGrade() {
        Double total = getTotalScore();
        return total != null ? GradeLevel.fromScore(total) : null;
    }
}