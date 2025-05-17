package com.menghor.ksit.feature.attendance.models;

import com.menghor.ksit.enumations.SubmissionStatus;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.school.model.ScheduleEntity;
import com.menghor.ksit.utils.database.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "score_sessions")
@Getter
@Setter
public class ScoreSessionEntity extends BaseEntity {
    
    @ManyToOne
    @JoinColumn(name = "schedule_id", nullable = false)
    private ScheduleEntity schedule;
    
    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    private UserEntity teacher;
    
    @ManyToOne
    @JoinColumn(name = "reviewer_id")
    private UserEntity reviewer;
    
    @Column(name = "submission_date")
    private LocalDateTime submissionDate;
    
    @Column(name = "review_date")
    private LocalDateTime reviewDate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubmissionStatus status = SubmissionStatus.DRAFT;
    
    @Column(name = "teacher_comments")
    private String teacherComments;
    
    @Column(name = "staff_comments")
    private String staffComments;
    
    @OneToMany(mappedBy = "scoreSession", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudentScoreEntity> studentScores = new ArrayList<>();
}