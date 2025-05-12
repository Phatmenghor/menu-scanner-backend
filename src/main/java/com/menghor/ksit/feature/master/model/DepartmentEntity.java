package com.menghor.ksit.feature.master.model;

import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.school.model.CourseEntity;
import com.menghor.ksit.feature.school.model.ScheduleEntity;
import com.menghor.ksit.utils.database.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
@Table(
        name = "departments",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_department_code", columnNames = "code"),
                @UniqueConstraint(name = "uk_department_name", columnNames = "name")
        }
)
public class DepartmentEntity extends BaseEntity {

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    private String urlLogo;

    @Enumerated(EnumType.STRING)
    private Status status;

    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL)
    private List<MajorEntity> majors;

    // Students enrolled in this class
    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL)
    private List<UserEntity> students = new ArrayList<>();

    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL)
    private List<CourseEntity> courses = new ArrayList<>();
}
