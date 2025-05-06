package com.menghor.ksit.feature.auth.models;

import com.menghor.ksit.utils.database.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

// ប្រវត្តិការងារបន្តបន្ទាប់
@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "teacher_experience")
public class TeacherExperienceEntity extends BaseEntity {

    private String continuousEmployment; // ការងារបន្តបន្ទាប់
    private String workPlace; // អង្គភាពបម្រើការងារបច្ចុប្បន្ន
    private LocalDate startDate;
    private LocalDate endDate;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;
}