package com.menghor.ksit.feature.auth.dto.relationship;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeacherLanguageDto {
    private Long id; // Optional, used for updates
    private String language; // ផ្នែភាសា
    private String reading; // ការអាន
    private String writing; // ការសរសេរ
    private String speaking; // ការសន្ទនា
}
