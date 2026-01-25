package com.emenu.features.auth.mapper;

import com.emenu.features.auth.dto.helper.UserSessionCreateHelper;
import com.emenu.features.auth.models.UserSession;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct mapper for UserSession entity
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserSessionMapper {

    /**
     * Create UserSession from helper DTO - pure MapStruct mapping
     */
    UserSession createFromHelper(UserSessionCreateHelper helper);
}
