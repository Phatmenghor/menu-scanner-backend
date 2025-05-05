package com.menghor.ksit.feature.master.model;

import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.utils.database.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "departments")
public class DepartmentEntity extends BaseEntity {

    private String code;
    private String name;
    private String url_logo;

    @Enumerated(EnumType.STRING)
    private Status status;

    @OneToMany(mappedBy = "department")
    private List<MajorEntity> majors;


}
