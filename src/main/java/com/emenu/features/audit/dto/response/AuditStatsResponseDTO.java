package com.emenu.features.audit.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditStatsResponseDTO {

    private Long totalLogs;
    private Long last24Hours;
    private Long last7Days;
}
