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
public class TeachersProfessionalRankDto {
    private Long id; // Optional, used for updates
    private String typeOfProfessionalRank; // ប្រភេទឋានៈវិជ្ជាជីវៈ
    private String description; // បរិយាយ
    private String announcementNumber; // ប្រកាសលេខ
    private LocalDate dateAccepted; // កាលបរិច្ឆេទទទួល
}
