package com.menghor.ksit.feature.auth.dto.relationship;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeacherShortCourseDto {
    private Long id; // Optional, used for updates
    private String skill; // ផ្នែក
    private String skillName; // ឈ្មោះជំនាញ
    private LocalDate startDate; // ថ្ងៃចាប់ផ្តើម
    private LocalDate endDate; // ថ្ងៃបញ្ចប់
    private String duration; // រយៈពេល
    private String preparedBy; // រៀបចំដោយ
    private String supportBy; // គាំទ្រដោយ
}