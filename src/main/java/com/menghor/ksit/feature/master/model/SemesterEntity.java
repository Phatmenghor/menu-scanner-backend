package com.menghor.ksit.feature.master.model;

import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.utils.database.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
@Entity
@Table(name = "semester")
public class SemesterEntity extends BaseEntity {

    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer academyYear;

    @Enumerated(EnumType.STRING)
    private Status status;
}
