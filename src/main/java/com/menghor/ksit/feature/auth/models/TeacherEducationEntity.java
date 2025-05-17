package com.menghor.ksit.feature.auth.models;

import com.menghor.ksit.utils.database.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

// បកម្រិតវប្បធម៌
@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "teacher_education")
public class TeacherEducationEntity extends BaseEntity {

    private String culturalLevel; // កម្រិតវប្បធម៌
    private String skillName; // ឈ្មោះជំនាញ
    private LocalDate dateAccepted; // កាលបរិច្ឆេទទទួល
    private String country; // ប្រទេស

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;
}


