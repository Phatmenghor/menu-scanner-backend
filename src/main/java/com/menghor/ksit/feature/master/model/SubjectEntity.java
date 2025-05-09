package com.menghor.ksit.feature.master.model;

import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.feature.school.model.CourseEntity;
import com.menghor.ksit.utils.database.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "subjects")
public class SubjectEntity extends BaseEntity {

    private String name;

    @Enumerated(EnumType.STRING)
    private Status status;

    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL)
    private List<CourseEntity> courses;
}
