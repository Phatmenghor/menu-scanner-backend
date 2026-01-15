package com.emenu.features.enums.dto.update;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkScheduleTypeEnumUpdateRequest {
    private String enumName;
    private String description;
}