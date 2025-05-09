package com.menghor.ksit.feature.course.model;

import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.master.model.DepartmentEntity;
import com.menghor.ksit.feature.master.model.SubjectEntity;
import com.menghor.ksit.utils.database.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "courses")
public class CourseEntity extends BaseEntity {

    private String code;
    private String nameKH;
    private String nameEn;

    private Integer credit; // Number of credit hours.
    private Integer theory; // Hours of theory classes.
    private Integer execute; // Hours of practical execution.
    private Integer apply;  // Hours applied in practice.
    private Integer totalHour;
    private String description;
    private String purpose;
    private String expectedOutcome;

    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private DepartmentEntity department;

    @ManyToOne
    @JoinColumn(name = "subject_id")
    private SubjectEntity subject;

    @ManyToOne
    @JoinColumn(name = "teacher_id")
    private UserEntity user;
}
