package com.menghor.ksit.feature.auth.dto.resposne;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Paginated response for student users
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentUserAllResponseDto {
    private List<StudentUserResponseDto> content;
    private int pageNo;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean last;
}