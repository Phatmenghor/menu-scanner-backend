package com.emenu.features.audit.dto.filter;

import com.emenu.shared.dto.BaseFilterRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogFilterDTO extends BaseFilterRequest {
    private UUID userId;
    private String userIdentifier;
    private String userType;
}
