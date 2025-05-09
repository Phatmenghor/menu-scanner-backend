package com.menghor.ksit.feature.master.dto.response;

import com.menghor.ksit.enumations.Status;
import lombok.Data;

import java.time.LocalDate;

@Data
public class SemesterResponseDto {
    private Long id;
    private String semester;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer academyYear;
    private Status status;
}
