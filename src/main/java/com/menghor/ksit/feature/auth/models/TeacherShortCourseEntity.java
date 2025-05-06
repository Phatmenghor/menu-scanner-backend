package com.menghor.ksit.feature.auth.models;

import com.menghor.ksit.utils.database.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

// វគ្គខ្លីៗ
@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "teacher_short_course")
public class TeacherShortCourseEntity extends BaseEntity {
    private String skill; // ផ្នែក
    private String skillName; // ឈ្មោះជំនាញ
    private LocalDate startDate; // ថ្ងៃចាប់ផ្តើម
    private LocalDate endDate; // ថ្ងៃបញ្ចប់
    private String duration; // រយៈពេល
    private String preparedBy ; // រៀបចំដោយ
    private String supportBy; // គាំទ្រដោយ

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;
}


