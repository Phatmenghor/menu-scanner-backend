package com.menghor.ksit.feature.master.model;

import com.menghor.ksit.enumations.DegreeEnum;
import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.enumations.YearLevelEnum;
import com.menghor.ksit.utils.database.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "classes")
public class ClassEntity extends BaseEntity {

    private String code;

    @Enumerated(EnumType.STRING)
    private DegreeEnum degree;          // bachelor

    @Enumerated(EnumType.STRING)
    private YearLevelEnum yearLevel;    // first year

    @Enumerated(EnumType.STRING)
    private Status status;

    private Integer academyYear;         // 2025, 2026

    @ManyToOne
    @JoinColumn(name = "major_id")
    private MajorEntity major;
}
