package com.menghor.ksit.feature.attendance.dto.response;

import com.menghor.ksit.enumations.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoreConfigurationResponseDto {
    private Long id;
    private BigDecimal attendancePercentage;
    private BigDecimal assignmentPercentage;
    private BigDecimal midtermPercentage;
    private BigDecimal finalPercentage;
    private BigDecimal totalPercentage;
    private Status status;
    private String createdAt;
}