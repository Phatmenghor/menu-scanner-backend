package com.menghor.ksit.feature.school.model;

import com.menghor.ksit.enumations.DayOfWeek;
import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.enumations.YearLevelEnum;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.master.model.ClassEntity;
import com.menghor.ksit.feature.master.model.RoomEntity;
import com.menghor.ksit.feature.master.model.SemesterEntity;
import com.menghor.ksit.feature.survey.model.SurveyEntity;
import com.menghor.ksit.utils.database.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "schedule")
public class ScheduleEntity extends BaseEntity {

    private LocalTime startTime;

    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Enumerated(EnumType.STRING)
    private DayOfWeek day;
    
    @Enumerated(EnumType.STRING)
    private YearLevelEnum yearLevel;

    @ManyToOne
    @JoinColumn(name = "classes_id")
    private ClassEntity classes;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private CourseEntity course;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private RoomEntity room;

    @ManyToOne
    @JoinColumn(name = "semester_id")
    private SemesterEntity semester;

    // Add bidirectional relationship with surveys
    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SurveyEntity> surveys = new ArrayList<>();
}