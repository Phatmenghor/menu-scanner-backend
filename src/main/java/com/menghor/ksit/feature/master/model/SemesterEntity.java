package com.menghor.ksit.feature.master.model;

import com.menghor.ksit.enumations.Semester;
import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.feature.school.model.ScheduleEntity;
import com.menghor.ksit.utils.database.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "semester")
public class SemesterEntity extends BaseEntity {

    private String semesterCode;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer academyYear;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Enumerated(EnumType.STRING)
    private Semester semester;

    @OneToMany(mappedBy = "semester", cascade = CascadeType.ALL)
    private List<ScheduleEntity> schedules = new ArrayList<>();
}
