package com.emenu.features.enums.dto.response;

import com.emenu.shared.dto.BaseAuditResponse;
import lombok.*;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkScheduleTypeEnumResponse extends BaseAuditResponse {
    private UUID id;
    private UUID businessId;
    private String enumName;
    private String description;
}