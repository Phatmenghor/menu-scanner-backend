package com.menghor.ksit.feature.auth.dto.relationship;

import com.menghor.ksit.enumations.EducationLevelEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudentStudiesHistoryDto {
    private Long id; // Optional, used for updates
    private EducationLevelEnum typeStudies; // កម្រិតថ្នាក់
    private String schoolName; // ឈ្មោះសាលារៀន
    private String location; // ឈខេត្ត/រាជធានី
    private LocalDate fromYear; // ពីឆ្នាំណា
    private LocalDate endYear; // ដល់ឆ្នាំណា
    private String obtainedCertificate; // សញ្ញាបត្រទទួលបាន
    private String overallGrade; // និទ្ទេសរួម
}
