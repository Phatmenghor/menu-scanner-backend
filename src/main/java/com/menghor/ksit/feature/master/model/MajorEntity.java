package com.menghor.ksit.feature.master.model;

import com.menghor.ksit.enumations.Status;
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
        name = "majors",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_major_code", columnNames = "code")
        }
)
public class MajorEntity extends BaseEntity {

    @Column(name = "code", nullable = false, unique = true)
    private String code;
    private String name;

    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private DepartmentEntity department;

    @OneToMany(mappedBy = "major", cascade = CascadeType.ALL)
    private List<ClassEntity> classes;

}
