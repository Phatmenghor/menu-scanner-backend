package com.menghor.ksit.feature.attendance.models;

import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.utils.database.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "score_configurations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScoreConfigurationEntity extends BaseEntity {

    @Column(name = "attendance_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal attendancePercentage;

    @Column(name = "assignment_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal assignmentPercentage;

    @Column(name = "midterm_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal midtermPercentage;

    @Column(name = "final_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal finalPercentage;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.ACTIVE;

    @Transient
    public BigDecimal getTotalPercentage() {
        return attendancePercentage
                .add(assignmentPercentage)
                .add(midtermPercentage)
                .add(finalPercentage);
    }

    @Transient
    public boolean isValidConfiguration() {
        return getTotalPercentage().compareTo(BigDecimal.valueOf(100)) == 0;
    }
}