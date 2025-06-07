package com.menghor.ksit.feature.attendance.models;

import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.utils.database.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "score_configurations")
@Data
@EqualsAndHashCode(callSuper = true)
public class ScoreConfigurationEntity extends BaseEntity {

    @Column(name = "attendance_percentage", nullable = false)
    private Integer attendancePercentage;

    @Column(name = "assignment_percentage", nullable = false)
    private Integer assignmentPercentage;

    @Column(name = "midterm_percentage", nullable = false)
    private Integer midtermPercentage;

    @Column(name = "final_percentage", nullable = false)
    private Integer finalPercentage;

    @Column(name = "total_percentage", nullable = false)
    private Integer totalPercentage = 100;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.ACTIVE;

    // Calculate total percentage
    @PostLoad
    @PostPersist
    @PostUpdate
    public void calculateTotalPercentage() {
        if (attendancePercentage != null && assignmentPercentage != null &&
                midtermPercentage != null && finalPercentage != null) {
            this.totalPercentage = attendancePercentage + assignmentPercentage +
                    midtermPercentage + finalPercentage;
        }
    }

    // Validation method
    public boolean isValidConfiguration() {
        return getTotalPercentage() != null && getTotalPercentage().equals(100);
    }

    // Getter for total percentage calculation
    public Integer getTotalPercentage() {
        if (attendancePercentage == null || assignmentPercentage == null ||
                midtermPercentage == null || finalPercentage == null) {
            return 0;
        }
        return attendancePercentage + assignmentPercentage + midtermPercentage + finalPercentage;
    }
}