package com.emenu.features.enums.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveStatusEnumCreateRequest {
    @NotNull(message = "Business ID required")
    private UUID businessId;
    
    @NotBlank(message = "Enum name required")
    private String enumName;
    
    private String description;
}