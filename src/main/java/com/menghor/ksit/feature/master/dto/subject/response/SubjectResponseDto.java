package com.menghor.ksit.feature.master.dto.subject.response;

import com.menghor.ksit.enumations.Status;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SubjectResponseDto {
    private Long id;
    private String name;
    private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
