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
public class TeacherVocationalDto {
    private Long id; // Optional, used for updates
    private String culturalLevel; // កម្រិតវិជ្ជាជីវៈ
    private String skillOne; // ឯកទេសទី១
    private String skillTwo; // ឯកទេសទី២
    private String trainingSystem; // ប្រព័ន្ធបណ្តុះបណ្តាល
    private LocalDate dateAccepted; // ថ្ងៃខែបានទទួល
}