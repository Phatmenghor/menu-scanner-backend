package com.menghor.ksit.feature.master.dto.response;

import com.menghor.ksit.enumations.DegreeEnum;
import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.enumations.YearLevelEnum;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ClassResponseDto {
    private Long id;
    private String code;
    private Integer academyYear;
    private DegreeEnum degree;
    private YearLevelEnum yearLevel;
    private Status status;
    private MajorResponseDto major;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
