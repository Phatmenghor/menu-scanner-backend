package com.menghor.ksit.feature.school.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ScheduleBulkDuplicateRequestDto {
    
    // Source (FROM)
    @NotNull(message = "Source class ID is required")
    private Long sourceClassId;
    
    @NotNull(message = "Source semester ID is required")
    private Long sourceSemesterId;
    
    // Target (TO)
    @NotNull(message = "Target class ID is required")
    private Long targetClassId;
    
    @NotNull(message = "Target semester ID is required")
    private Long targetSemesterId;
}
