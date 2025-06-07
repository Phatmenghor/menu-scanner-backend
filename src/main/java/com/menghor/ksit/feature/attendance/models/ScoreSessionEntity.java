package com.menghor.ksit.feature.attendance.models;

import com.menghor.ksit.enumations.SubmissionStatus;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.school.model.ScheduleEntity;
import com.menghor.ksit.utils.database.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "score_sessions")
@Data
@EqualsAndHashCode(callSuper = true)
public class ScoreSessionEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private ScheduleEntity schedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private UserEntity teacher;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SubmissionStatus status = SubmissionStatus.DRAFT;

    @Column(name = "submission_date")
    private LocalDateTime submissionDate;

    @Column(name = "teacher_comments", columnDefinition = "TEXT")
    private String teacherComments;

    @Column(name = "staff_comments", columnDefinition = "TEXT")
    private String staffComments;

    @OneToMany(mappedBy = "scoreSession", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<StudentScoreEntity> studentScores;
}