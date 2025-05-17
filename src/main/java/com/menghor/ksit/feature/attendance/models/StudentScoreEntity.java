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
    private Double attendanceScore; // Automatically calculated from attendance, 10%
    
    @Column(name = "assignment_score")
    private Double assignmentScore; // Manual input, 20%
    
    @Column(name = "midterm_score")
    private Double midtermScore; // Manual input, 30%
    
    @Column(name = "final_score")
    private Double finalScore; // Manual input, 40%
    
    @Column(name = "total_score")
    private Double totalScore; // Calculated
    
    @Enumerated(EnumType.STRING)
    private GradeLevel grade; // Determined from total score
    
    @Column(name = "comments")
    private String comments;
    
    // Helper methods for score calculations
    public void calculateTotalScore() {
        // Define weights
        double attendanceWeight = 0.10; // 10%
        double assignmentWeight = 0.20; // 20%
        double midtermWeight = 0.30;    // 30% 
        double finalWeight = 0.40;      // 40%
        
        // Calculate total score
        double total = 0.0;
        if (attendanceScore != null) total += attendanceScore * attendanceWeight;
        if (assignmentScore != null) total += assignmentScore * assignmentWeight;  
        if (midtermScore != null) total += midtermScore * midtermWeight;
        if (finalScore != null) total += finalScore * finalWeight;
        
        this.totalScore = total;
        this.calculateGrade();
    }
    
    private void calculateGrade() {
        if (this.totalScore != null) {
            this.grade = GradeLevel.fromScore(this.totalScore);
        }
    }
}