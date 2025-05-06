package com.menghor.ksit.feature.auth.models;

import com.menghor.ksit.enumations.EducationLevelEnum;
import com.menghor.ksit.utils.database.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

// ប្រវត្តិការសិក្សា
@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "student_studies_history")
public class StudentStudiesHistoryEntity extends BaseEntity {

    @Enumerated(EnumType.STRING)
    private EducationLevelEnum typeStudies; // កម្រិតថ្នាក់

    private String schoolName; // ឈ្មោះសាលារៀន
    private String location; // ឈខេត្ត/រាជធានី
    private LocalDate fromYear; // ពីឆ្នាំណា
    private LocalDate endYear; // ដល់ឆ្នាំណា

    private String obtainedCertificate; // សញ្ញាបត្រទទួលបាន
    private String overallGrade; // និទ្ទេសរួម

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;
}


