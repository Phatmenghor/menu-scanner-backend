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
public class TeacherPraiseOrCriticismDto {
    private Long id; // Optional, used for updates
    private String typePraiseOrCriticism; // ប្រភេទនៃការសរសើរ/ការស្តីបន្ទោស/ទទួលអធិការកិច្ច
    private String giveBy; // ផ្តល់ដោយ
    private LocalDate dateAccepted; // កាលបរិច្ឆេទទទួល
}