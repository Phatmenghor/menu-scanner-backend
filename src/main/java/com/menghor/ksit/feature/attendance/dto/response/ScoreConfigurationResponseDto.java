package com.menghor.ksit.feature.attendance.dto.response;

import com.menghor.ksit.enumations.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Data
public class ScoreConfigurationResponseDto {
    private Long id;
    private Integer attendancePercentage;
    private Integer assignmentPercentage;
    private Integer midtermPercentage;
    private Integer finalPercentage;
    private Integer totalPercentage;
    private Status status;
    private String createdAt;
}