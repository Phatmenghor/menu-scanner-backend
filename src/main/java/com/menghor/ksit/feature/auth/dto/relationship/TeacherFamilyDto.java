package com.menghor.ksit.feature.auth.dto.relationship;

import com.menghor.ksit.enumations.GenderEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeacherFamilyDto {
    private Long id; // Optional, used for updates
    private String nameChild; // ឈ្មោះកូន
    private GenderEnum gender; // ភេទ
    private LocalDate dateOfBirth; // ថ្ងៃខែឆ្នាំកំណើត
    private String working; // មុខរបរ
}