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
public class StudentSiblingDto {
    private Long id; // Optional, used for updates
    private String name; // ឈ្មោះបងប្អូន
    private GenderEnum gender; // ភេទ
    private LocalDate dateOfBirth; // ថ្ងៃខែឆ្នាំកំណើត
    private String occupation; // មុខរបរ
    private String phoneNumber; // លេខទូរស័ព្ទ
    private String address; // អាសយដ្ឋាន
}