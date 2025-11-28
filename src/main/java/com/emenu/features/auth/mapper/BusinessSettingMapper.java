package com.emenu.features.auth.mapper;

import com.emenu.features.auth.dto.request.BusinessSettingCreateRequest;
import com.emenu.features.auth.dto.response.BusinessSettingResponse;
import com.emenu.features.auth.dto.update.BusinessSettingUpdateRequest;
import com.emenu.features.auth.models.BusinessSetting;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BusinessSettingMapper {

    @Mapping(target = "businessName", source = "business.name")
    BusinessSettingResponse toResponse(BusinessSetting businessSetting);

    BusinessSetting toEntity(BusinessSettingCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(BusinessSettingUpdateRequest request, @MappingTarget BusinessSetting businessSetting);
}