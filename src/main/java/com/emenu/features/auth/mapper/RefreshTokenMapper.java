package com.emenu.features.auth.mapper;

import com.emenu.features.auth.dto.helper.RefreshTokenCreateHelper;
import com.emenu.features.auth.models.RefreshToken;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct mapper for RefreshToken entity
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RefreshTokenMapper {

    /**
     * Create RefreshToken from helper DTO - pure MapStruct mapping
     */
    RefreshToken createFromHelper(RefreshTokenCreateHelper helper);
}
