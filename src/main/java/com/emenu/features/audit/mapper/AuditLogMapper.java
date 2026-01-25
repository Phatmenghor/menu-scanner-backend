package com.emenu.features.audit.mapper;

import com.emenu.features.audit.dto.helper.AuditLogCreateHelper;
import com.emenu.features.audit.models.AuditLog;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct mapper for AuditLog entity
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AuditLogMapper {

    /**
     * Create AuditLog from helper DTO - pure MapStruct mapping
     */
    AuditLog createFromHelper(AuditLogCreateHelper helper);
}
