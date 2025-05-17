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
public class TeacherEducationDto {
    private Long id; // Optional, used for updates
    private String culturalLevel; // កម្រិតវប្បធម៌
    private String skillName; // ឈ្មោះជំនាញ
    private LocalDate dateAccepted; // កាលបរិច្ឆេទទទួល
    private String country; // ប្រទេស
}