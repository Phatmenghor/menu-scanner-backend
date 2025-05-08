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
public class TeacherExperienceDto {
    private Long id; // Optional, used for updates
    private String continuousEmployment; // ការងារបន្តបន្ទាប់
    private String workPlace; // អង្គភាពបម្រើការងារបច្ចុប្បន្ន
    private LocalDate startDate;
    private LocalDate endDate;
}
